import networkx as nx
from matplotlib import pyplot as plt
import scipy as sp
import xlrd
import json
import csv
import math
import os
import pandas as pd


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

# 读取电信节点
def read_ct_node_list(file_name):
    data = xlrd.open_workbook(file_name)
    table = data.sheets()[0]
    rows = table.nrows
    ct_as_list = []
    for i in range(rows):
        if i == 0:
            continue
        if table.row_values(i)[3] == "ChinaTelecom":
            ct_as_list.append(table.row_values(i)[1])
    return ct_as_list

# 按国家聚合
def aggregation(node, line, dic):
    national_node = []
    national_line = []
    national_node_location = []
    for n_i in node:
        if n_i in dic.keys() and dic[n_i]['ca'] not in national_node:
            tmp_tup = (dic[n_i]['ca'], dic[n_i]['la'], dic[n_i]['lo'])
            national_node_location.append(tmp_tup)
            national_node.append(dic[n_i]['ca'])
    for l_i in line:
        if l_i[0] not in dic.keys() or l_i[1] not in dic.keys():
            continue
        if dic[l_i[0]]['ca'] == dic[l_i[1]]['ca']:  # 国家内部连接隐藏
            continue
        tup = (dic[l_i[0]]['ca'], dic[l_i[1]]['ca'])
        if tup not in national_line:
            national_line.append(tup)
    # 纯节点列表， 节点坐标列表， 线路列表
    return national_node, national_node_location, national_line


# 国内各节点与国外连接的节点数
def total_china_node(node, dic):
    result = []
    for n in node:
        if n in dic.keys() and dic[n]['ca'] == 'China':
            result.append(n)
        if n == '23764':
            result.append(n)
    return result


def one_china_skip(node, line, dic):
    one_skip_dic = {}
    for l_i in line:
        if l_i[0] not in dic.keys() or l_i[1] not in dic.keys():
            continue
        if dic[l_i[0]]['ca'] != 'China' and dic[l_i[1]]['ca'] != 'China' and \
                l_i[0] != '23764' and l_i[1] != '23764':  # 国家内部连接隐藏
            continue
        if dic[l_i[0]]['ca'] == 'China' and dic[l_i[1]]['ca'] == 'China':  # 国家内部连接隐藏
            continue
        if l_i[0] == '23764' and dic[l_i[1]]['ca'] == 'China':  # 国家内部连接隐藏
            continue
        if l_i[1] == '23764' and dic[l_i[1]]['ca'] == 'China':  # 国家内部连接隐藏
            continue

        # 记录节点连接数
        china_node_id = l_i[0] if dic[l_i[0]]['ca'] == 'China' else l_i[1]
        if china_node_id not in one_skip_dic:
            tmp = [(l_i[0] if l_i[0] != china_node_id else l_i[1], math.pow(2, l_i[2]))]
            one_skip_dic[china_node_id] = {'list': tmp, 'count': 1}
        else:
            tmp = one_skip_dic[china_node_id]
            tmp_id = l_i[0] if l_i[0] != china_node_id else l_i[1]
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
            one_skip_dic[china_node_id] = tmp
    return one_skip_dic


def two_skip(node, line, dic):
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
    return two_skip_dic

# 创造G6数据集 带坐标
def build_g6_xy(node, line, filename):
    g6_node = []
    g6_line = []
    for n_i in node:
        tmp = {'id': n_i[0], 'label': n_i[0], 'x': float(n_i[1]), 'y': float(n_i[2]), 'cluster': "a"}
        g6_node.append(tmp)
    for l_i in line:
        tmp = {'source': l_i[0], 'target': l_i[1]}
        g6_line.append(tmp)
    json_result = {'nodes': g6_node, 'edges': g6_line}
    with open(filename, 'w') as file_obj:
        json.dump(json_result, file_obj)


if __name__ == "__main__":
    a = read_big_file("/Users/mac/desktop/bgpdump_full_snapshot.txt")
    ct_list = read_ct_node_list("/Users/mac/desktop/as_info.xls")
    as_location_dict = read_as_location("/Users/mac/desktop/as_country_with_geo.csv")
    data_size = 10000

    count = len(open("/Users/mac/desktop/bgpdump_full_snapshot.txt", 'r').readlines())
    print("监测数据总量：", count)

    print(" ")
    print("监测到的节点总数：", len(as_location_dict))
    print("当前监测的数据集大小：", data_size)

    node_list, alone_node, line_list = build_dataset(a, data_size)
    print("     线路数量：", len(line_list))
    print("     独立节点集合：", alone_node)
    print("     节点数量：", len(node_list))

    # 节点社团聚合
    # national_node_list, node_location_list, national_line_list = aggregation(node_list, line_list, as_location_dict)
    # print("当前监测范围内国家地区数量为：", len(national_node_list))
    # print("当前监测范围内国家地区数量为：", len(node_location_list))
    # print("当前监测范围内国家地区间线路为：", len(national_line_list))

    # 国内各节点与国外连接的节点数
    print(" ")
    conn_for_dict = one_china_skip(node_list, line_list, as_location_dict)
    total_china_node_list = total_china_node(node_list, as_location_dict)
    print("其有", len(total_china_node_list), "个国内节点")
    # print("与国外连接的国内节点个数：", len(conn_for_dict))
    # print("国内各节点与国外连接情况：", conn_for_dict)
    sort_list = sorted(conn_for_dict.items(), key=lambda x: x[1]['count'], reverse=True)
    print("与国外连接情况：")
    print("共有", len(sort_list), "个节点与国外相连")
    ct_num = 0
    for it in sort_list:
        if it[0] in ct_list:
            ct_num = ct_num + 1
    print("其中电信节点有", ct_num, "个：")
    result_csv_list = []
    # print(sort_list[10])
    ct_num = 1
    two_skip_list = []
    for it in sort_list:
        if it[0] not in ct_list:
            continue
        ip_size = 0
        for it_in in it[1]['list']:
            ip_size += it_in[1]
            print(" ", it[0], "——",  it_in[1], "——", it_in[0], "(", as_location_dict[it_in[0]]['ca'], ")")
            result_csv_list.append([it[0], it_in[0], it_in[1], 1])
            if it_in[0] not in two_skip_list:
                two_skip_list.append(it_in[0])
        print(ct_num, " ", it[0], "与", it[1]['count'], "个国外节点相连，总IP连接量为：", ip_size)
        ct_num = ct_num + 1

    # 第二跳
    two_skip_dic = two_skip(two_skip_list, line_list, as_location_dict)
    sort_two_list = sorted(two_skip_dic.items(), key=lambda x: x[1]['count'], reverse=True)
    print("共有", len(sort_list), "个节点参与第二跳计算")
    two_num = 1
    two_skip_list = []
    for it in sort_two_list:
        ip_size = 0
        for it_in in it[1]['list']:
            ip_size += it_in[1]
            print(it[0], "——", it_in[1], "——", it_in[0], "(", as_location_dict[it_in[0]]['ca'], ")")
            result_csv_list.append([it[0], it_in[0], it_in[1], 2])
            if it_in[0] not in two_skip_list:
                two_skip_list.append(it_in[0])
        print(two_num, " ", it[0], "(", as_location_dict[it[0]]['ca'], ")",  "与", it[1]['count'],
              "个国外节点相连，总IP连接量为：", ip_size)
        two_num = two_num + 1

    # 保存json
    # json_file = "/Users/mac/desktop/g6data_xy.json"
    # build_g6_xy(node_location_list, national_line_list, json_file)

    # 绘图
    # G = nx.DiGraph()
    # G.add_nodes_from(national_node_list)
    # G.add_edges_from(national_line_list)
    # sort_list = sorted(G.degree(), key=lambda x: x[1], reverse=True)
    # print("当前网络度：", sort_list)
    # pos = nx.kamada_kawai_layout(G)
    # nx.draw_networkx(G, pos, node_size=10, node_color='red', edge_color='blue')
    # plt.show()

    # 保存为csv
    df1 = pd.DataFrame(data=result_csv_list, columns=['begin', 'end', 'size', 'k'])
    df1.to_csv('/Users/mac/desktop/result_713.csv', index=False)
