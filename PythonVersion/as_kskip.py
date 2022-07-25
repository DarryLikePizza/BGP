import networkx as nx
import numpy as np
from matplotlib import pyplot as plt
import scipy as sp
import xlrd
import json
import csv
import math
import os
import pandas as pd

"""
相对于kslip_only_first的修改
1. 对同IP段做去重
"""

# 逐行读取
def read_big_file(fpath):
    with open(fpath, "r", encoding="utf8") as f:
        while True:
            line = f.readline()
            if not line:
                break
            yield line


# 构造数据集
"""
    str_i: 线路字符串
    pow_i: ip段
    k_i：跳数
"""
def build_k_edge(str_i, pow_i, ip, k_i):
    as_conn_list = []  # 边集
    as_node_list = []  # 节点集

    str_as_list = str_i.split(' ')
    # 制作连接关系集
    if str_as_list[0] not in as_node_list:
        as_node_list.append(str_as_list[0])
    k = 0
    for it_index in range(len(str_as_list) - 1):
        if str_as_list[it_index] == str_as_list[it_index + 1]:
            continue
        k = k + 1
        tup = (str_as_list[it_index], str_as_list[it_index + 1], pow_i, ip)  # 7.14 添加ip段
        as_conn_list.append(tup)
        if str_as_list[it_index + 1] not in as_node_list:
            as_node_list.append(str_as_list[it_index + 1])
        if k == k_i:
            break
    return as_node_list, as_conn_list


# ------------------ ksip ------------------------
def read_csv_file(fpath, start_id):
    # 读取csv
    # 读取表头
    head_row = pd.read_csv(fpath, nrows=0)
    # 表头列转为 list
    head_row_list = list(head_row)
    # 读取
    csv_result = pd.read_csv(fpath, usecols=head_row_list)
    result = csv_result.loc[csv_result['begin'] == start_id]
    result_list = np.array(result).tolist()
    return result_list


# as坐标字典/国家坐标字典
def read_as_location(file_name):
    with open(file_name) as f:
        f_csv = csv.reader(f)
        headers = next(f_csv)
        as_loc_dic = {}
        for row in f_csv:
            tmp_dic = {'ca': row[1], 'la': row[2], 'lo': row[3]}
            as_loc_dic[row[0][2:]] = tmp_dic
    return as_loc_dic

def k_skip(node_list, line, dic, diff, as_original):
    skip_dic = {}
    for l_i in line:
        if l_i[0] not in dic.keys() or l_i[1] not in dic.keys():
            continue
        if l_i[0] not in node_list:
            continue
        if dic[l_i[0]]['ca'] == dic[l_i[1]]['ca']:  # 国家内部连接隐藏
            continue
        if diff is False and dic[l_i[1]]['ca'] == dic[as_original]['ca']:  # 7.14 不包含源点国
            continue

        # 记录节点连接数
        node = l_i[0]
        if node not in skip_dic:
            ip_list = [l_i[3], ]
            tmp = [(l_i[1], math.pow(2, l_i[2]), ip_list)]
            skip_dic[node] = {'list': tmp, 'count': 1}
        else:
            tmp = skip_dic[node]
            tmp_id = l_i[1]
            flag = 0
            for it_in_list in tmp['list']:
                if tmp_id == it_in_list[0]:
                    if l_i[3] in it_in_list[2]:  # 7.14 如果ip相同则去重
                        continue
                    # 7.14 存储ip列表
                    cache_tup = it_in_list[1] + math.pow(2, l_i[2])
                    cache_ip_list = it_in_list[2].copy()
                    cache_ip_list.append(l_i[3])
                    tmp['list'].remove((it_in_list[0], it_in_list[1], it_in_list[2]))
                    tmp['list'].append((it_in_list[0], cache_tup, cache_ip_list))
                    flag = 1
                    break  # 7.14 提前出循环
            if flag == 0:
                tup_tmp = (tmp_id, math.pow(2, l_i[2]), [l_i[3], ])
                tmp['list'].append(tup_tmp)
                tmp['count'] = tmp['count'] + 1
            skip_dic[node] = tmp
    return skip_dic


def disp_info(dic, csv_list, info_dic, dp, k):
    # 第一跳结果展示
    print("第", k, "跳输出信息：")
    node_list = []
    one_num = 1
    sorted_list = sorted(dic.items(), key=lambda x: x[1]['count'], reverse=True)
    for sl in sorted_list:
        ip_sum = 0
        ip_count = 0
        for sl_in in sl[1]['list']:
            ip_sum += sl_in[1]
            ip_count += len(sl_in[2])
            if dp is True:
                # 7.14 也输出ip段数
                print(" ", sl[0], "——", int(sl_in[1]), "(", len(sl_in[2]), "段IP)——", sl_in[0], "(", info_dic[sl_in[0]]['ca'],")")
            csv_list.append([sl[0], info_dic[sl[0]]['ca'], sl_in[0], info_dic[sl_in[0]]['ca'], sl_in[1], sl_in[2], 1])
            if sl_in[0] not in node_list:
                node_list.append(sl_in[0])
        print(one_num, " ", sl[0], "(", info_dic[sl[0]]['ca'], ")",
              "与", sl[1]['count'], "个国外节点相连，总IP连接量为：", int(ip_sum), "，总IP段量为：", int(ip_count))
        one_num = one_num + 1
    return csv_list, node_list


"""
    want_as: 要求的as
    csv_path: 生成的全量数据csv文件
    as_info_path：as描述信息csv文件
    k：跳数，目前只支持1跳和2跳
    disp: 是否显示连接细节
    diff：第二条是否包含源AS国
"""
def as_kskip(want_as, csv_path, as_info_path, save_path, k, disp, diff):
    as_list = read_csv_file(csv_path, want_as)
    as_location_dict = read_as_location(as_info_path)

    # 获得k跳路径
    k_list = [[] for index in range(k)]
    for ai in as_list:
        node_list, line_list = build_k_edge(ai[1], ai[3], ai[2], k)  # 7.14 ai[2]查重
        for lli in range(len(line_list)):
            k_list[lli].append(line_list[lli])

    # 计算k跳信息
    result_csv_list = []
    node_list = [str(want_as)]
    for i in range(k):
        k_skip_dic = k_skip(node_list, k_list[i], as_location_dict, diff, str(want_as))
        result_csv_list, node_list = disp_info(k_skip_dic, result_csv_list, as_location_dict, disp, i + 1)

    # 保存为csv
    df1 = pd.DataFrame(data=result_csv_list, columns=['begin', 'begin_ca', 'end', 'end_ca', 'ip_size', 'ip_list', 'k'])
    df1.to_csv(save_path, index=False)



if __name__ == "__main__":
    wa = 54574
    watch_path = "/Users/mac/desktop/snapshot.csv"
    as_info_path = "/Users/mac/desktop/as_country_with_geo.csv"
    save_path = '/Users/mac/desktop/result_713_demo.csv'
    k = 5
    disp = False
    diff = True
    as_kskip(wa, watch_path, as_info_path, save_path, k, disp, diff)

