def read_ca_list(file_name, ca_name):
    with open(file_name) as f:
        f_csv = csv.reader(f)
        headers = next(f_csv)
        as_list = []
        as_loc_dic = {}
        for row in f_csv:
            if row[1] == ca_name:
                as_list.append(row[0][2:])

            tmp_dic = {'ca': row[1], 'la': row[2], 'lo': row[3]}
            as_loc_dic[row[0][2:]] = tmp_dic
    return as_list, as_loc_dic

# 一个国家所有as的线路和pow数据
def read_csv_ca(fpath, as_list):
    # 读取csv
    # 读取表头
    head_row = pd.read_csv(fpath, nrows=0)
    print(list(head_row))
    # 表头列转为 list
    head_row_list = list(head_row)
    # 读取
    csv_result = pd.read_csv(fpath, usecols=head_row_list)
    result = csv_result.loc[csv_result['begin'].isin(as_list)]
    result_list = np.array(result).tolist()
    return result_list