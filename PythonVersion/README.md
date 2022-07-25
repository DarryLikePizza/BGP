# BGP前端接口数据格式



##### 1、饼状图

```json
{
    "area": [
        {"area_name": "Japan", "IP_count": 10000},
        {"area_name": "United States", "IP_count": 30000}
    ],
    "as_detail": [
        {"area_name": "Japan",
         "as_list":[
            {"as_no": "214", "IP_count": 4000},
             {"as_no": "215", "IP_count": 6000}
        ]},
        {"area_name": "United States",
         "as_list":[
            {"as_no": "314", "IP_count": 14000},
             {"as_no": "315", "IP_count": 16000}
        ]}
    ]
}
```



##### 2、引力图

```json
{
    "pointData":[
	{
		"id":1,
		"asNumber":"14315",
		"value":[-100.445882, 39.7837304],
		"country":"United States",
		"province":"",
		"city":"",
		"coefficient":1,
	},
	{
		"id":2,
		"asNumber":"73",
		"value":[-100.445882, 39.7837304],
		"country":"United States",
		"province":"",
		"city":"",
		"coefficient":1,
	}],
    "lineData":[
	{
		"source":1,
        "target":2,
		"coefficient":1
	}]
}
```

其中：point1为中心查询节点

##### 3、地图

```json
{
    "pointData":[
	{
		"id":1,
		"as_list":["22", "33"],
		"value":[-100.445882, 39.7837304],
		"country":"United States",
		"province":"",
		"city":"",
		"coefficient":1,
	},
	{
		"id":2,
		"as_list":["73", "65"],
		"value":[119.333, 39.78373042],
		"country":"China",
		"province":"",
		"city":"",
		"coefficient":1,
	}],
    "lineData":[
	{
		"point":[1, 2],
		"coefficient":3,
        "coords":[[-100.445882, 39.7837304], [119.333, 39.78373042]]
	}]
}

```

