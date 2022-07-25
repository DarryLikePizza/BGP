import networkx as nx
from matplotlib import pyplot as plt
import scipy as sp
import xlrd
import json
import csv
import math
import os
import pandas as pd

"""
相对于kslip的修改
1. 模块化
2. 全量数据
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
def build_dataset(fpath, line_count):
    as_conn_list = []  # 边集
    alone_node = []  # 孤立节点
    as_node_list = []  # 节点集
    for i in range(line_count):
        # 读取
        str_or = next(fpath).strip()
        str_list = str_or.split('|', 7)
        two_pow = 32 - int(str_list[5].split('/')[1])
        str_as_list = str_list[6].split(' ')
        # 制作连接关系集
        if str_as_list[0] not in alone_node and len(str_as_list) == 1:
            alone_node.append(str_as_list[0])
        elif str_as_list[0] in alone_node and len(str_as_list) > 1:
            alone_node.remove(str_as_list[0])
        if str_as_list[0] not in as_node_list:
            as_node_list.append(str_as_list[0])
        for it_index in range(len(str_as_list) - 1):
            if str_as_list[it_index] == str_as_list[it_index + 1]:
                continue
            tup = (str_as_list[it_index], str_as_list[it_index + 1], two_pow)  # 没有对0做清除处理
            as_conn_list.append(tup)
            if str_as_list[it_index + 1] in alone_node:
                alone_node.remove(str_as_list[it_index + 1])
            if str_as_list[it_index + 1] not in as_node_list:
                as_node_list.append(str_as_list[it_index + 1])
    return as_node_list, alone_node, as_conn_list


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


# 国内各节点与国外连接的节点数
def total_china_node(node, dic):
    result = []
    for n in node:
        if n in dic.keys() and dic[n]['ca'] == 'China':
            result.append(n)
        if n == '23764':
            result.append(n)
    return result

def one_skip(node, line, dic):
    one_skip_dic = {}
    ca = dic[node]['ca']
    print(ca)
    for l_i in line:
        if l_i[0] not in dic.keys() or l_i[1] not in dic.keys():
            continue
        if l_i[0] != node and l_i[1] != node:
            continue
        if dic[l_i[0]]['ca'] == ca and l_i[1] == node:  # 国家内部连接隐藏
            continue
        if dic[l_i[1]]['ca'] == ca and l_i[0] == node:  # 国家内部连接隐藏
            continue

        # 记录节点连接数
        if node not in one_skip_dic:
            tmp = [(l_i[0] if l_i[0] != node else l_i[1], math.pow(2, l_i[2]))]
            one_skip_dic[node] = {'list': tmp, 'count': 1}
        else:
            tmp = one_skip_dic[node]
            tmp_id = l_i[0] if l_i[0] != node else l_i[1]
            flag = 0
            for it_in_list in tmp['list']:
                if tmp_id == it_in_list[0]:
                    cache_tup = it_in_list[1] + math.pow(2, l_i[2])
                    tmp['list'].remove((it_in_list[0], it_in_list[1]))
                    tmp['list'].append((it_in_list[0], cache_tup))
                    flag = 1
            if flag == 0:
                tup_tmp = (tmp_id, math.pow(2, l_i[2]))
                tmp['list'].append(tup_tmp)
                tmp['count'] = tmp['count'] + 1
            one_skip_dic[node] = tmp
    return one_skip_dic


"""
    start_node: 要查找的节点
    line: 线路集合
    dic: 节点字典
    disp: 是否在控制台显示连接细节
"""
def two_skip_total(start_node, line, dic, disp):
    one_skip_dic = one_skip(start_node, line, dic)

    # 第一跳结果展示
    print("第一跳输出信息：")
    node = []
    one_num = 1
    result_csv_list = []
    sorted_list = sorted(one_skip_dic.items(), key=lambda x: x[1]['count'], reverse=True)
    for sl in sorted_list:
        ip_sum = 0
        for sl_in in sl[1]['list']:
            ip_sum += sl_in[1]
            if disp is True:
                print(" ", sl[0], "——", sl_in[1], "——", sl_in[0], "(", as_location_dict[sl_in[0]]['ca'], ")")
            result_csv_list.append([sl[0], as_location_dict[sl[0]]['ca'],
                                    sl_in[0], as_location_dict[sl_in[0]]['ca'],
                                    sl_in[1],  1])
            if sl_in[0] not in node:
                node.append(sl_in[0])
        print(one_num, " ", sl[0], "(", as_location_dict[sl[0]]['ca'], ")",
              "与", sl[1]['count'], "个国外节点相连，总IP连接量为：", ip_sum)
        one_num = one_num + 1

    # 第二跳基于第一跳节点集
    print(" ")
    two_skip_dic = {}
    for l_i in line:
        if l_i[0] not in dic.keys() or l_i[1] not in dic.keys():
            continue
        if dic[l_i[0]]['ca'] == dic[l_i[1]]['ca']:  # 国家内部连接隐藏
            continue
        if l_i[0] not in node and l_i[1] not in node:
            continue
        # 记录节点连接数
        find_node_id = l_i[0] if l_i[0] in node else l_i[1]
        if find_node_id not in two_skip_dic:
            tmp = [(l_i[0] if l_i[0] != find_node_id else l_i[1], math.pow(2, l_i[2]))]
            two_skip_dic[find_node_id] = {'list': tmp, 'count': 1}
        else:
            tmp = two_skip_dic[find_node_id]
            tmp_id = l_i[0] if l_i[0] != find_node_id else l_i[1]
            flag = 0
            for it_in_list in tmp['list']:
                if tmp_id == it_in_list[0]:
                    cache_tup = it_in_list[1] + math.pow(2, l_i[2])
                    tmp['list'].remove((it_in_list[0], it_in_list[1]))
                    tmp['list'].append((it_in_list[0], cache_tup))
                    flag = 1
            if flag == 0:
                tup_tmp = (tmp_id, math.pow(2, l_i[2]))
                tmp['list'].append(tup_tmp)
                tmp['count'] = tmp['count'] + 1
            two_skip_dic[find_node_id] = tmp

    # 第二跳结果显示
    print("第二跳输出信息：")
    sort_two_list = sorted(two_skip_dic.items(), key=lambda x: x[1]['count'], reverse=True)
    two_num = 1
    two_skip_list = []
    for it in sort_two_list:
        ip_size = 0
        for it_in in it[1]['list']:
            ip_size += it_in[1]
            if disp is True:
                print(it[0], "——", it_in[1], "——", it_in[0], "(", as_location_dict[it_in[0]]['ca'], ")")
            result_csv_list.append([it[0], as_location_dict[it[0]]['ca'],
                                    it_in[0], as_location_dict[it_in[0]]['ca'],
                                    it_in[1], 2])
            if it_in[0] not in two_skip_list:
                two_skip_list.append(it_in[0])
        print(two_num, " ", it[0], "(", as_location_dict[it[0]]['ca'], ")", "与", it[1]['count'],
              "个国外节点相连，总IP连接量为：", ip_size)
        two_num = two_num + 1
    return result_csv_list


if __name__ == "__main__":
    a = read_big_file("/Users/mac/desktop/bgpdump_full_snapshot.txt")
    as_location_dict = read_as_location("/Users/mac/desktop/as_country_with_geo.csv")
    data_size = 10000

    count = len(open("/Users/mac/desktop/bgpdump_full_snapshot.txt", 'r').readlines())
    node_list, alone_node, line_list = build_dataset(a, data_size)
    print("监测数据总量：", count)
    print("监测节点总数：", len(as_location_dict))
    print("当前监测的数据集大小：", data_size)
    print("        线路数量：", len(line_list))
    print("        独立节点集合：", alone_node)
    print("        节点数量：", len(node_list))

    # 二跳
    # start_node: 要查找的节点
    # line: 线路集合
    # dic: 节点字典
    # disp: 是否在控制台显示连接细节
    want_as = '13335'
    result_csv_list = two_skip_total(want_as, line_list, as_location_dict, False)

    # 保存为csv
    df1 = pd.DataFrame(data=result_csv_list, columns=['begin', 'begin_ca', 'end', 'end_ca', 'size', 'k'])
    df1.to_csv('/Users/mac/desktop/result_713_demo.csv', index=False)
