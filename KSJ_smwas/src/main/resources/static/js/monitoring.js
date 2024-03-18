



let monitoringData = null;

function fetchMonitoringData() {
    fetch('/monitoring')
        .then(response => response.json())
        .then(response => {
            monitoringData = response;
        })
        .catch(error =>
            console.error('Error:', error),
            monitoringData = null       // 전역변수에 데이터 저장
            );
}


fetchMonitoringData();
drawCpuChart();
drawMemoryChart();
drawDiskChart();
getSessionList();




// 1초마다 모니터링 데이터 가져오기
setInterval(fetchMonitoringData, 1000);


/**
 * CPU 차트
 */
async function drawCpuChart() {

    const onChartLoad = function () {
        const chart = this,
            series = chart.series[0];

        setInterval(async function () {
            const x = (new Date()).getTime(); // current time
            const y = monitoringData ? monitoringData.cpu * 100 : 0;     // 저장된 전역변수 값을 가져옴 
            series.addPoint([x, y], true, true);
            $('#cpuInfo').text(y.toFixed(2));
        }, 1000);
    };

    const data = (function () {
        const data = [];
        const time = new Date().getTime();

        for (let i = -119; i <= 0; i += 1) {    // 총 120개 보여짐 (2분)
            data.push({
                x: time + i * 1000,
                y: null
            });
        }
        return data;
    }());

    Highcharts.chart('cpuChart', {
        chart: {
            type: 'area',
            events: {
                load: onChartLoad
            }
        },
        time: {
            useUTC: false
        },
        /* 차트명 */
        title: {
            text: ''
        },
        /* X축 */
        xAxis: {
            type: 'datetime',
            tickPixelInterval: 150,
            maxPadding: 0.1
        },
        /* Y축 */
        yAxis: {
            title: {
                text: 'CPU (%)'
            },
            min: 0,
            max: 100,
            plotLines: [
                {
                    value: 0,
                    width: 1,
                    color: '#808080'
                }
            ]
        },
        /* 범례 속성 */
        legend: {
            enabled: true
        },
        /*  Highcharts의 출처 정보 */
        credits: {
            enabled: false
        },
        plotOptions: {
            area: {
                fillColor: {
                    linearGradient: { x1: 0, y1: 0, x2: 0, y2: 1 },
                    stops: [
                        [0, Highcharts.getOptions().colors[0]],
                        [1, Highcharts.color(Highcharts.getOptions().colors[0]).setOpacity(0).get('rgba')]
                    ]
                },
                marker: {
                    radius: 2
                },
                lineWidth: 1,
                states: {
                    hover: {
                        lineWidth: 1
                    }
                },
                threshold: null
            }
        },
        /* 범례, 데이터 값 */
        series: [
            {
                name: 'CPU',
                lineWidth: 2,
                //color: Highcharts.getOptions().colors[2],
                data
            }
        ],
        tooltip: {
            headerFormat: '<b>{series.name}</b><br/>',
            pointFormat: '{point.x:%Y-%m-%d %H:%M:%S}<br/>{point.y:.2f} %'
        }
    });
}


/**
 * Memory 차트
 */
async function drawMemoryChart() {

    const onChartLoad = function () {
        const chart = this,
            series1 = chart.series[0], // Heap Memory
            series2 = chart.series[1]; // Non Heap Memory

        const updateData = async function () {
            const x = (new Date()).getTime(); // current time
            const y1 = monitoringData ? monitoringData.Heapsize : 0;
            const y2 = monitoringData ? monitoringData.usedMemory : 0;

            //addPoint(점, redraw.shift, animation)
            series1.addPoint([x, y1], true, true);
            series2.addPoint([x, y2], true, true);

            $('#heapSizeInfo').text(Number(y1).toLocaleString());
            $('#usedHeapInfo').text(Number(y2).toLocaleString());
        };
        setInterval(updateData, 1000);
    };

    const data = (function () {
        const data = [];
        const time = new Date().getTime();

        for (let i = -119; i <= 0; i += 1) {    // 총 120개 보여짐 (2분)
            data.push({
                x: time + i * 1000,
                y1: null,
                y2: null
            });
        }
        return data;
    }());



    Highcharts.chart('memoryChart', {
        chart: {
            type: 'area',
            events: {
                load: onChartLoad
            }
        },
        time: {
            useUTC: false
        },
        title: {
            text: ''
        },
        xAxis: {
            type: 'datetime',
            tickPixelInterval: 150,
            maxPadding: 0.1
        },
        yAxis: {
            title: {
                text: 'Memory (B)'
            },
            // min: 0,
            // max: 200
        },
        legend: {
            enabled: true
        },
        credits: {
            enabled: false
        },
        plotOptions: {
            area: {
                marker: {
                    radius: 2
                },
                lineWidth: 1,
                states: {
                    hover: {
                        lineWidth: 1
                    }
                },
                threshold: null,
            },
        },
        series: [
            {
                name: 'Heap Size',
                lineWidth: 2,
                data: data.map(item => [item.x, item.y1]),
                color: Highcharts.getOptions().colors[2],
                fillColor: {
                    linearGradient: { x1: 0, y1: 0, x2: 0, y2: 1 },
                    stops: [
                        [0, Highcharts.color(Highcharts.getOptions().colors[2]).setOpacity(0.3).get('rgba')], // 시작 색상
                        [1, Highcharts.color(Highcharts.getOptions().colors[2]).setOpacity(0).get('rgba')] // 끝 색상 (투명)
                    ]
                }
            },
            {
                name: 'Used Heap',
                lineWidth: 2,
                data: data.map(item => [item.x, item.y2]),
                color: Highcharts.getOptions().colors[0],
                fillColor: '#dbeefc'
            },
        ],
        tooltip: {
            headerFormat: '<b>{series.name}</b><br/>',
            pointFormat: '{point.x:%Y-%m-%d %H:%M:%S}<br/>{point.y} B'
        }
    });
}


/**
 *  Disk 차트
 */
async function drawDiskChart() {
    
    const fetchedData = async function () {
        var using = monitoringData ? monitoringData.usingDisk : 0;
        var usable = monitoringData ? monitoringData.usableDisk : 0;
        
        $('#usingDiskInfo').text(Number(using).toLocaleString());
        $('#usableDiskInfo').text(Number(usable).toLocaleString());
        
        return [
            ['Using', using],
            ['Usable', usable]
        ];
    };
    
    
    const onChartLoad = function () {
        const chart = this,
            series = chart.series[0];
        // 주기적으로 데이터 업데이트
        setInterval(async function () {
            const newData = await fetchedData();
            series.setData(newData);
        }, 1000); 
    };

    Highcharts.chart('diskChart', {
        chart: {
            plotBackgroundColor: null,
            plotBorderWidth: 0,
            plotShadow: false,
            events: {
                load: onChartLoad      // async 안됨
            }
        },
        title: {
            text: ''
        },
        credits: {
            enabled: false
        },
        plotOptions: {
            pie: {
                dataLabels: {
                    enabled: true,
                    format: '<b>{point.name}</b><br>{point.percentage:.1f} %',
                    distance: -50,
                    style: {
                        fontWeight: 'bold',
                        color: 'white'
                    }
                },
                startAngle: -90,
                endAngle: 90,
                center: ['50%', '75%'],
                size: '130%'
            }
        },
        series: [{
            type: 'pie',
            innerSize: '50%',
            borderRadius: 5,
            data: fetchedData,        // async 안됨
            colors: [
                Highcharts.getOptions().colors[0],
                '#0ff3a0'
            ]
        }],
        tooltip: {
            headerFormat: '',
            pointFormat:
                '<span style="color:{point.color}">\u25CF</span> <b> {point.name}</b><br/>'
                + '{point.percentage:.1f}%<br/>'
                + '{point.y} MB'
        },
    });

    
}


/**
 *  웹소켓 세션 리스트
 */
async function getSessionList() {
    if (!monitoringData){
        setTimeout(getSessionList, 500);
    }

    // {connection: 1, ipList: Array(0)}
    var sessionList = monitoringData ? monitoringData.sessionList : {connection: 0, ipList: []};

    let status = document.getElementById("status");
    let ipAddress = document.getElementById("ipAddress");
    
    if (sessionList.connection == 0) {
        ipAddress.innerText = "session is not connected";
        if (status.classList.contains('using')) {
            status.classList.replace('using', 'usable');
        }
    } else {
        if (sessionList.ipList[0] == null) {
            ipAddress.innerText = "waiting...";
        } else {
	        let ipListString = "";
	        for (let ip of sessionList.ipList) {
				ipListString += ip + "<br/>";
	        }
	        ipAddress.innerHTML = ipListString;
	        
	        if (status.classList.contains('usable')) {
	            status.classList.replace('usable', 'using');
	        }
			
		}
    }

    setTimeout(getSessionList, 1000) // 1초 후 재실행


}