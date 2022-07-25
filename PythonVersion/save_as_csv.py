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
保存为csv文件
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
def save_set(fpath, line_count):
    as_info_list = []
    for i in range(line_count):
        # 读取
        str_or = next(fpath).strip()
        str_list = str_or.split('|', 7)
        two_pow = 32 - int(str_list[5].split('/')[1])
        str_as_list = str_list[6].split(' ')
        tmp_list = [str_list[4], str_list[6], str_list[5], two_pow]
        as_info_list.append(tmp_list)
    return as_info_list


if __name__ == "__main__":
    a = read_big_file("/Users/mac/desktop/bgpdump_full_snapshot.txt")
    count = len(open("/Users/mac/desktop/bgpdump_full_snapshot.txt", 'r').readlines())
    # data_size = count
    data_size = 10000

    print("监测数据总量：", count)
    print("当前监测的数据集大小：", data_size)

    result_csv_list = save_set(a, data_size)
    # 保存为csv
    df1 = pd.DataFrame(data=result_csv_list, columns=['begin', 'line', 'ip', 'pow'])
    df1.to_csv('/Users/mac/desktop/snapshot.csv', index=False)