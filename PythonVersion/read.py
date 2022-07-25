import networkx as nx
from matplotlib import pyplot as plt
import scipy as sp
import xlrd
import json
import csv


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
            tup = (str_as_list[it_index], str_as_list[it_index + 1])
            as_conn_list.append(tup)
            if str_as_list[it_index + 1] in alone_node:
                alone_node.remove(str_as_list[it_index + 1])
            if str_as_list[it_index + 1] not in as_node_list:
                as_node_list.append(str_as_list[it_index + 1])
    return as_node_list, alone_node, as_conn_list


# 读取节点标识
def read_xlr(file_name):
    data = xlrd.open_workbook(file_name)
    table = data.sheets()[0]
    rows = table.nrows
    as_name_dic = {}
    as_nation_dic = {}
    for i in range(rows):
        if i == 0:
            continue
        # 增as_name_dic
        if as_name_dic.get(table.row_values(i)[0]) is None:
            tmp_list = [table.row_values(i)[1]]
        else:
            tmp_list = as_name_dic[table.row_values(i)[0]]
            tmp_list.append(table.row_values(i)[1])
        as_name_dic[table.row_values(i)[0]] = tmp_list
        as_nation_dic[table.row_values(i)[1]] = table.row_values(i)[0]
    return as_name_dic, as_nation_dic


# 读取国家经纬度
def read_location(file_name):
    data = xlrd.open_workbook(file_name)
    table = data.sheets()[0]
    rows = table.nrows
    as_loc_dic = {}
    for i in range(rows):
        if i == 0:
            continue
        la_str = table.row_values(i)[2]
        la = ''
        if la_str[-1] == 'S':
            la += '-'
        la += la_str.split('°')[0] + '.' + la_str.split('°')[1][0:2]
        lo_str = table.row_values(i)[3]
        lo = ''
        if lo_str[-1] == 'W':
            lo += '-'
        lo = lo_str.split('°')[0] + '.' + lo_str.split('°')[1][0:2]
        tmp_dic = {'ca': table.row_values(i)[1], 'la': la, 'lo': lo}
        as_loc_dic[table.row_values(i)[0]] = tmp_dic
    print(as_loc_dic)

# as坐标字典/国家坐标字典
def read_as_location(file_name):
    with open(file_name) as f:
        f_csv = csv.reader(f)
        headers = next(f_csv)
        as_loc_dic = {}
        for row in f_csv:
            tmp_dic = {'ca': row[1], 'la': row[2], 'lo': row[3]}
            as_loc_dic[row[0][2:]] = tmp_dic
    print(len(as_loc_dic))
    return as_loc_dic

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
    return national_node, national_node_location, national_line


# 创造G6数据集
def build_g6(node, line, filename):
    g6_node = []
    g6_line = []
    for n_i in node:
        tmp = {'id': n_i, 'label': n_i, 'cluster': "a"}
        g6_node.append(tmp)
    for l_i in line:
        tmp = {'source': l_i[0], 'target': l_i[1]}
        g6_line.append(tmp)
    json_result = {'nodes': g6_node, 'edges': g6_line}
    with open(filename, 'w') as file_obj:
        json.dump(json_result, file_obj)


# 带坐标
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
    # as_dic, as_nation_dic = read_xlr("/Users/mac/desktop/as_info.xls")
    # read_location("/Users/mac/desktop/nation.xls")
    as_location_dict = read_as_location("/Users/mac/desktop/as_country_with_geo.csv")
    data_size = 1000

    # print(as_dic)
    # print("监测到的国家和地区总数：", len(as_dic))
    print("监测到的节点总数：", len(as_location_dict))
    # for as_key in as_dic:
    #     print(as_key, "有", len(as_dic[as_key]), "个节点")
    print("当前监测的数据集大小：", data_size)

    node_list, alone_node, line_list = build_dataset(a, data_size)

    # print(line_list)
    print("当前监测访问内线路数量：", len(line_list))
    print("当前监测范围内独立节点集合：", alone_node)
    # print(node_list)
    print("当前监测范围内节点数量：", len(node_list))

    # 节点社团聚合
    national_node_list, node_location_list, national_line_list = aggregation(node_list, line_list, as_location_dict)
    print("当前监测范围内国家数量为：", len(national_node_list))
    print("当前监测范围内国家数量为：", len(node_location_list))
    print("当前监测范围内国家间线路为：", len(national_line_list))
    print(national_node_list)
    print(node_location_list)
    print(national_line_list)

    # 保存json
    json_file = "/Users/mac/desktop/g6data_xy.json"
    build_g6_xy(node_location_list, national_line_list, json_file)

    # 绘图
    G = nx.DiGraph()
    G.add_nodes_from(national_node_list)
    G.add_edges_from(national_line_list)
    sort_list = sorted(G.degree(), key=lambda x: x[1], reverse=True)
    print("当前网络度：", sort_list)
    # pos = nx.kamada_kawai_layout(G)
    # nx.draw_networkx(G, pos, node_size=10, node_color='red', edge_color='blue')
    # plt.show()
