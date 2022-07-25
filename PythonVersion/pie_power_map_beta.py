import numpy as np
import json
import csv
import math
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
                print(" ", sl[0], "——", int(sl_in[1]), "(", len(sl_in[2]), "段IP)——", sl_in[0], "(",
                      info_dic[sl_in[0]]['ca'], ")")
            csv_list.append([sl[0], info_dic[sl[0]]['ca'], sl_in[0], info_dic[sl_in[0]]['ca'], sl_in[1], sl_in[2], k])
            if sl_in[0] not in node_list:
                node_list.append(sl_in[0])
        print(one_num, " ", sl[0], "(", info_dic[sl[0]]['ca'], ")",
              "与", sl[1]['count'], "个国外节点相连，总IP连接量为：", int(ip_sum), "，总IP段量为：", int(ip_count))
        one_num = one_num + 1
    return csv_list, node_list


# ------------------ country_rank ------------------------
# 一个国家的所有节点列表
def read_ca_list(file_name, ca_name):
    with open(file_name) as f:
        f_csv = csv.reader(f)
        headers = next(f_csv)
        as_list = []
        as_loc_dic = {}
        for row in f_csv:
            if row[1] == ca_name:
                as_list.append(str(row[0][2:]))

            tmp_dic = {'ca': row[1], 'la': row[2], 'lo': row[3]}
            as_loc_dic[row[0][2:]] = tmp_dic
    return as_list, as_loc_dic


# 一个国家所有as的线路和pow数据
def read_csv_ca(fpath, as_list):
    # 读取csv
    # 读取表头
    head_row = pd.read_csv(fpath, nrows=0)
    # 表头列转为 list
    head_row_list = list(head_row)
    # 读取
    csv_result = pd.read_csv(fpath, usecols=head_row_list)
    sa_int_list = []
    for it in as_list:
        sa_int_list.append(int(it))
    result = csv_result.loc[csv_result['begin'].isin(sa_int_list)]
    result_list = np.array(result).tolist()
    return result_list


def ca_one_skip(node_list, line, dic, diff, as_original):
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
            skip_dic[node] = {'list': tmp, 'count': 1, 'sum': math.pow(2, l_i[2])}
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
                    tmp['sum'] = tmp['sum'] + math.pow(2, l_i[2])
                    flag = 1
                    break  # 7.14 提前出循环
            if flag == 0:
                tup_tmp = (tmp_id, math.pow(2, l_i[2]), [l_i[3], ])
                tmp['list'].append(tup_tmp)
                tmp['count'] = tmp['count'] + 1
                tmp['sum'] = tmp['sum'] + math.pow(2, l_i[2])
            skip_dic[node] = tmp
    return skip_dic


"""
    多跳方法
    :param
        want_as: 要求的as
        csv_path: 生成的全量数据csv文件
        as_info_path：as描述信息csv文件
        k：跳数，目前只支持1跳和2跳
        disp: 是否显示连接细节
        diff：第二条是否包含源AS国
"""


def as_kskip(want_as, csv_path, as_info_path, k, disp, diff):
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
    # df1.to_csv(save_path, index=False)
    return df1


"""
    国家排名方法
    :param
        ca 要查询的国家
        csv_path 生成的全量数据csv文件
        as_info_path as描述信息csv文件
        save_path  最终数据保存路径
        disp  是否在控制台输出详细信息
"""


def ca_rank(ca, csv_path, as_info_path, save_path, disp):
    as_list, as_location_dict = read_ca_list(as_info_path, ca)
    as_ca_list = read_csv_ca(csv_path, as_list)
    # 1. 字典 as_location_dict
    # 2. 国家as列表 as_list
    # 3. 线路列表 as_ca_list

    # 获得一跳所有路径
    one_list = []
    for ai in as_ca_list:
        node_list, line_list = build_k_edge(ai[1], ai[3], ai[2], 1)
        for lli in range(len(line_list)):
            one_list.append(line_list[lli])

    result_list = []
    result_dic = ca_one_skip(as_list, one_list, as_location_dict, False, str(as_list[0]))
    if result_dic:
        for kk, vv in result_dic.items():
            result_list.append((kk, vv))

    # 第二跳结果显示
    print(ca, "的国外连接节点信息：")
    print("共", len(result_list), "个节点")
    sort_list = sorted(result_list, key=lambda x: x[1]['sum'], reverse=True)
    count = 1
    csv_list = []
    for it in sort_list:
        for it_in in it[1]['list']:
            if disp is True:
                print(it[0], "——", int(it_in[1]), "——", it_in[0], "(", as_location_dict[it_in[0]]['ca'], ")")
            csv_list.append([it[0], as_location_dict[it[0]]['ca'],
                             it_in[0], as_location_dict[it_in[0]]['ca'],
                             int(it_in[1]), 1])
        print(count, " ", it[0], "(", as_location_dict[it[0]]['ca'], ")", "与", it[1]['count'],
              "个国外节点相连，总IP连接量为：", int(it[1]['sum']))
        count = count + 1

    # 保存为csv
    df1 = pd.DataFrame(data=csv_list, columns=['begin', 'begin_ca', 'end', 'end_ca', 'size', 'k'])
    df1.to_csv(save_path, index=False)
    return csv_list, df1


# 饼状图数据提取与构造
# skip_table： DataFrame格式
# k： 要提取的跳数
# limit_count： 整合为others国家的临界值
# limit_count： 整合为others节点的临界值
def get_k_skip_pie_data(skip_table, k, limit_count, limit_count_for_node):
    if k == 0:
        one_skip_table = skip_table
    else:
        one_skip_table = skip_table.loc[skip_table['k'] == k]  # 一跳国家数据
    end_ca = one_skip_table['end_ca'].drop_duplicates()
    ca_list = np.array(end_ca).tolist()
    # 组合数据
    area_list = []
    as_detail_list = []
    for a in ca_list:
        result = one_skip_table.loc[one_skip_table['end_ca'] == a]
        ip_sum = result.iloc[:, 4].sum(axis=0)

        # 715 小于一定值的归为其他
        if int(ip_sum) < limit_count:
            a = 'others'
            flag = 0
            for i in range(len(area_list)):
                if area_list[i]['area_name'] == 'others':
                    area_list[i]['IP_count'] += int(ip_sum)
                    flag = 1
                    break
            if flag == 0:
                area_list.append({"area_name": a, "IP_count": int(ip_sum)})
        else:
            area_list.append({"area_name": a, "IP_count": int(ip_sum)})

        as_list = []
        for index, row in result.iterrows():
            # 715 相同目标源点加和
            tmp = {"as_no": str(row['end']), "IP_count": int(row['ip_size'])}
            same_flag = 0
            for item_tmp in as_list:
                if item_tmp['as_no'] == tmp['as_no']:
                    item_tmp['IP_count'] += tmp['IP_count']
                    same_flag = 1
                    break
            if same_flag == 0:
                as_list.append(tmp)

        # 715 小于一定值的归为其他
        if a == 'others':
            flag = 0
            for i in range(len(as_detail_list)):
                if as_detail_list[i]['area_name'] == 'others':
                    tmp = as_detail_list[i]['as_list'].copy()
                    # 715 相同目标源点加和
                    for item_as_list in as_list:
                        same_flag = 0
                        for item_tmp in tmp:
                            if item_tmp['as_no'] == item_as_list['as_no']:
                                item_tmp['IP_count'] += item_as_list['IP_count']
                                same_flag = 1
                                break
                        if same_flag == 0:
                            tmp.append(item_as_list)
                    sorted_tmp = sorted(tmp, key=lambda x: x['IP_count'], reverse=True)
                    as_detail_list[i]['as_list'] = sorted_tmp
                    flag = 1
                    break
            if flag == 0:
                as_sorted_list = sorted(as_list, key=lambda x: x['IP_count'], reverse=True)
                as_detail_list.append({"area_name": a, "as_list": as_sorted_list})
        else:
            as_sorted_list = sorted(as_list, key=lambda x: x['IP_count'], reverse=True)
            as_detail_list.append({"area_name": a, "as_list": as_sorted_list})

    # as_list中的小于limit_count_for_node的归为others
    for adl in as_detail_list:
        other_ip_count = 0
        del_list = []
        for al in adl['as_list']:
            if al['IP_count'] < limit_count_for_node:
                other_ip_count += al['IP_count']
                del_list.append(al)
        for dl in del_list:
            adl['as_list'].remove(dl)
        if other_ip_count != 0:
            adl['as_list'].append({'as_no': 'others', 'IP_count': other_ip_count})

    area_sorted_list = sorted(area_list, key=lambda x: x['IP_count'], reverse=True)
    result = {"area": area_sorted_list, "as_detail": as_detail_list}
    return result


"""
    饼状图
    :param
        wa 要查询的节点
        data_path 数据集路径
        as_info_path as信息集路径
        file_path_one 一跳信息存储路径
        file_path_two 二跳信息存储路径
        limit_count 国家聚合阈值
        limit_count_for_node 节点聚合阈值
    :return
        one_skip_result_json 一跳json数据
        two_skip_result_json 二跳json数据
    :output
        json文件
"""


def pie_disp(wa, skip_table, file_path_one, file_path_two, limit_count, limit_count_for_node):
    # 一跳
    one_skip_result = get_k_skip_pie_data(skip_table, 1, limit_count, limit_count_for_node)
    one_skip_result_json = json.dumps(one_skip_result)
    with open(file_path_one, 'w') as file_obj:
        json.dump(one_skip_result, file_obj)
    # 二跳
    two_skip_result = get_k_skip_pie_data(skip_table, 2, limit_count, limit_count_for_node)
    two_skip_result_json = json.dumps(two_skip_result)
    with open(file_path_two, 'w') as file_obj:
        json.dump(two_skip_result, file_obj)
    return one_skip_result_json, two_skip_result_json


"""
    引力图
    :param
        wa 要查询的节点
        data_path 数据集路径
        as_info_path as信息集路径
        file_path 信息存储路径
        size_limit 参数大小约束值
    :return
        result_json json数据
    :output
        json文件
"""


def power_disp(wa, as_dict, skip_table, file_path, size_limit):
    end_ca = skip_table['end'].drop_duplicates()
    ca_list = np.array(end_ca).tolist()
    ca_list.insert(0, str(wa))  # 添加源点

    point_list = []
    ca_index_dic = {}
    for ca_i in range(len(ca_list)):
        v_list = []
        find_v = as_dict[ca_list[ca_i]]
        v_list.append(float(find_v['la']))
        v_list.append(float(find_v['lo']))
        point_list.append({
            "asNumber": ca_list[ca_i],
            "value": v_list,
            "country": find_v['ca'],
            "province": "",
            "city": "",
            "coefficient": len(skip_table.loc[skip_table['end'] == ca_list[ca_i]]) if ca_i != 0 else 1,
        })
        ca_index_dic[ca_list[ca_i]] = ca_i

    line_list = []
    for index, row in skip_table.iterrows():
        src = ca_index_dic.get(str(row['begin']))
        tar = ca_index_dic.get(str(row['end']))

        # 去重
        flag = 0
        for ll in line_list:
            if ll['source'] == src and ll['target'] == tar:
                ll['coefficient'] += int(row['ip_size'])
                flag = 1
                break
        if flag == 0:
            line_list.append({
                "source": src,
                "target": tar,
                "coefficient": int(row['ip_size'])
            })

    for ll in line_list:
        int_length = len(str(ll['coefficient'])) - size_limit
        ll['coefficient'] = int_length if int_length > 0 else 1

    result = {"pointData": point_list, "lineData": line_list}
    result_json = json.dumps(result)
    with open(file_path, 'w') as file_obj:
        json.dump(result, file_obj)
    return result_json


"""
    地图
    :param
        wa 要查询的节点
        data_path 数据集路径
        as_info_path as信息集路径
        file_path 信息存储路径
        size_limit 参数大小约束值
    :return
        result_json json数据
    :output
        json文件
"""


def map_disp(wa, as_dict, skip_table, file_path, size_limit):
    # 提取所有国家
    end_ca = skip_table['end_ca'].drop_duplicates()
    ca_list = np.array(end_ca).tolist()
    # 组合数据
    ca_index_as_dic = {}  # ca, index, as_list, loc
    start_info = as_dict.get(str(wa))
    ca_index_as_dic[start_info['ca']] = (0, [str(wa)], [float(start_info['la']), float(start_info['lo'])], 1)
    for a_i in range(len(ca_list)):
        result = skip_table.loc[skip_table['end_ca'] == ca_list[a_i]]
        as_list = []
        for index, row in result.iterrows():
            as_list.append(str(row['end']))
        v_list = []
        find_v = as_dict[as_list[0]]
        v_list.append(float(find_v['la']))
        v_list.append(float(find_v['lo']))
        ca_index_as_dic[ca_list[a_i]] = (a_i+1, as_list, v_list, len(result))

    point_list = []
    for k, v in ca_index_as_dic.items():
        point_list.append({
            "id": v[0],
            "as_list": v[1],
            "value": v[2],
            "country": k,
            "province": "",
            "city": "",
            "coefficient": v[3],
        })

    line_list = []
    for index, row in skip_table.iterrows():
        begin_info = ca_index_as_dic.get(row['begin_ca'])
        end_info = ca_index_as_dic.get(row['end_ca'])
        # 去重
        flag = 0
        for ll in line_list:
            if ll['point'] == [begin_info[0], end_info[0]]:
                ll['coefficient'] += int(row['ip_size'])
                flag = 1
                break
        if flag == 0:
            line_list.append({
                "point": [begin_info[0], end_info[0]],
                "coefficient": int(row['ip_size']),
                "coords": [begin_info[2], end_info[2]]
            })

    for ll in line_list:
        int_length = len(str(ll['coefficient'])) - size_limit
        ll['coefficient'] = int_length if int_length > 0 else 1

    result = {"pointData": point_list, "lineData": line_list}
    result_json = json.dumps(result)
    with open(file_path, 'w') as file_obj:
        json.dump(result, file_obj)
    return result_json


if __name__ == "__main__":
    wa = 54574  # 搜索的as节点id
    info_path = "/Users/mac/desktop/as_country_with_geo.csv"  # as节点信息数据集
    data_path = "/Users/mac/desktop/snapshot.csv"  # as监控全量数据集
    k = 2
    disp = False
    diff = False
    as_dict = read_as_location(info_path)
    skip_table = as_kskip(wa, data_path, info_path, k, disp, diff)

    pie_save_path_one = "/Users/mac/desktop/pie_one_skip.json"
    pie_save_path_two = "/Users/mac/desktop/pie_two_skip.json"
    limit_count = 10000  # 国家聚合阈值
    node_limit_count = 100000  # 节点聚合阈值
    one_skip_pie_json, two_skip_pie_json = pie_disp(wa, skip_table, pie_save_path_one, pie_save_path_two,
                                                    limit_count, node_limit_count)
    print("饼状图：")
    print("一跳：")
    print(one_skip_pie_json)
    print("二跳：")
    print(two_skip_pie_json)

    power_save_path = "/Users/mac/desktop/power.json"
    size_limit = 2  # 边宽度约束值
    power_json = power_disp(wa, as_dict, skip_table, power_save_path, size_limit)
    print("引力图：")
    print(power_json)

    map_save_path = "/Users/mac/desktop/map.json"
    size_limit = 3  # 边宽度约束值
    map_json = map_disp(wa, as_dict, skip_table, map_save_path, size_limit)
    print("地图：")
    print(map_json)

    # print(" ")
    # print("国家排名：")
    # ca = 'United States'  # 要输出的国家
    # disp_r = False  # 是否显示细节
    # save_path = "/Users/mac/desktop/rank.csv"
    # ca_rank(ca, data_path, info_path, save_path, disp_r)

    # wa = 54574
    # k = 2
    # disp = False
    # diff = True
    # print(k, "跳信息：")
    # as_kskip(wa, data_path, info_path, k, disp, diff)

