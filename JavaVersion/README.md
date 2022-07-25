# 接口函数需求



#### 1、原始数据处理 

函数功能：由于国内国外数据不一致，将原始数据转换为统一的格式，提取出需要的字段。将转换好的数据存储为CSV的格式。

void dataPreProcess(String inPath, String outPath, Boolean isChinaData, String centralAs)

##### 国内数据示例：

字段格式：| route      | batch_no     | ip_type | ip_begin | ip_end   | contr | vr_client_ip | as_no | as_owner                   | isp  | country       | province | nexthop       | as_path         | best |

（注意：数据中是以tab分隔不是“|”,  as_path中没有带centralAs，需要在as_path前面添加上centralAs,）

```
1.0.64.0/18	NO2022070800	v4	16793600	16809983	163	42.123.69.67	18144	Energia Communications,Inc.	\N	Japan	\N	202.97.32.218	2497|7670|18144	1
```



##### 国外数据格式

```
TABLE_DUMP|1004140086|B|209.244.2.115|3356|3.0.0.0/8|3356 701 80|IGP|209.244.2.115|0|0|3356:3 3356:86 3356:575 3356:666 3356:668 3356:680 3356:2008|NAG||
```

各字段的含义，依次是： 

• BGP Protocol

• timestamp (in epoch format)

• W/A/B (withdrawal/announcement/routing table)

(withdrawal:BGP 退出表示先前宣布的前缀不可用。) 

• Peer IP 当前 collector 所在 AS 的一个 BGP 对等体的 IP（应该是当前 collector 的

一个邻居，它向这个 collecor 传播了此条路由） 

• Peer ASN 

• Prefix（起始 IP 段，ASPath 中最右侧 AS 号所包含的 IP 段） 

• ASPath

• Origin Protocol (typically always IGP)

路由的 Origin 属性代码。显示在每条路由的最后面。IGP：路由在起始 AS 的内部，使用 network 命令通过 BGP 通告路由时，其 Origin

属性为 IGP，用 i 表示。

EGP：通过 EGP 得到的路由信息，其 Origin 属性为 EGP，用 e 表示。

Incomplete：表示路由的来源无法确定。BGP 通过 import-route (BGP)命令引入的路

由，其 Origin 属性为 Incomplete。 

• Next Hop 当前 collector 所在 AS 要去往 prefix 的下一跳，应该就是 Peer IP

• LocalPref

本地优先级

• MED

（BGP 路由的 MED 度量值，作用类似于 IGP 路由的 Cost，也称为 Metric） 

• Community strings

• Atomic Aggregator

NAG 没自动聚合，AG 自动聚合

• Aggregator





#### 2、返回以观测点为中心的两跳内的信息

函数功能：输入一个AS号，返回从该AS出发的两跳内的连接关系，并记录每一跳的可到达网段数量。

例如：

输入AS 4134

若存在as_path：4134 564 873 678

则564 是一跳邻居，873为二条邻居，同时需要满足564 873不属于一个国家



#### 3、返回以任意节点为中心的两跳内的信息

函数功能：输入一个AS号，返回从该经由AS两跳内的连接关系，并记录每一跳的可到达网段数量。

例如输入AS 4134

若存在as_path：678 4134 564 873 785 （与上一接口的区别在于不用是起始节点）

则564 是一跳邻居，873为二条邻居，同时需要满足564 873不属于一个国家



#### 4、Json数据转换 

将数据转换为如下格式：



数据格式如下：

//饼状图
[
    {
    name: 'United Arab Emirates', //area_name 国家
    children: [
      {
        name: '15802', //as号
        value: 2230528, //IP_count
      },
      {
        name: '57187',
        value: 29952,
      },
      {
        name: 'others',
        value: 30720,
      },
    ],
  },
  {
    ......
  }
]

//地图
{
    points:[
        {
            id: 0,
            as_list: [
            '4134',
            ],
            value: [              //经纬度坐标
            104.999927,
            35.000074,
            ],
            country: 'China',
            province: '',
            city: '',
            coefficient: 1,
        },
    ],
    lines:[
        {
            from:'', //源id
            to:'',  //目的id
            coefficient: 3,
            coords: [
            [
                104.999927, 
                35.000074,
            ], //源经纬度
            [
                10.4234469,
                51.0834196,
            ], //目的经纬度
            ],
        },
        {}
    ]
}





