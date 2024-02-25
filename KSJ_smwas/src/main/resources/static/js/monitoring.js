



let monitoringData = null;

async function fetchMonitoringData() {
    await fetch('http://203.109.30.207:10001/monitoring')
        .then(response => response.json())
        .then(response => monitoringData = response)
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

    while (!monitoringData) {
        await new Promise(resolve => setTimeout(resolve, 100)); // 100밀리초(0.1초) 대기
    }

    const onChartLoad = function () {
        const chart = this,
            series = chart.series[0];

        setInterval(async function () {
            const x = (new Date()).getTime(); // current time
            const y = monitoringData.cpu * 100;     // 저장된 전역변수 값을 가져옴 
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

    while (!monitoringData) {
        await new Promise(resolve => setTimeout(resolve, 100)); // 100밀리초(0.1초) 대기
    }

    const onChartLoad = function () {
        const chart = this,
            series1 = chart.series[0], // Heap Memory
            series2 = chart.series[1]; // Non Heap Memory

        const updateData = async function () {
            const x = (new Date()).getTime(); // current time
            const y1 = monitoringData.Heapsize;
            const y2 = monitoringData.usedMemory;

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

    while (!monitoringData) {
        await new Promise(resolve => setTimeout(resolve, 100)); // 100밀리초(0.1초) 대기
    }

    const fetchedData = async function () {
        var using = monitoringData.usingDisk;
        var usable = monitoringData.usableDisk;

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

        setInterval(async function () {
            const newData = await fetchedData();
            series.setData(newData);
        }, 60000);  // 1분마다 호출
    };

    Highcharts.chart('diskChart', {
        chart: {
            plotBackgroundColor: null,
            plotBorderWidth: 0,
            plotShadow: false,
            events: {
                load: onChartLoad
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
            data: await fetchedData(),
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

    while (!monitoringData) {
        await new Promise(resolve => setTimeout(resolve, 100)); // 100밀리초(0.1초) 대기
    }
    var sessionList = monitoringData.sessionList;
    var tableBody = document.getElementById("sessionList");

    // 초기화
    while (tableBody.firstChild) {
        tableBody.removeChild(tableBody.firstChild);
    }

    for (var sessionItem in sessionList) {
        // sessionItem = "0" 이렇게 키 값으로 나옴 -> Integer 로 형변환 해서 index로 사용
        let itemNum = Number(sessionItem);
        var row = document.createElement("tr"); // <tr> 요소 생성

        // <td> 에 각 데이터 추가
        var statusCell = document.createElement("td");			// <td> 세션 사용 여부
        statusCell.classList.add('status');
        var websocketCell = document.createElement("td");	// <td> 세션 이용중인 웹소켓

        var circle = document.createElement("div");         // 원형 div 생성
        circle.classList.add("circle");
        /* 
            enable = true = usable = green
                    false = using = blue
        */
        if (sessionList[itemNum].webSocketId === null) {
            if (circle.classList.contains('using')) {
                circle.classList.replace('using', 'usable');
            }
            if (!circle.classList.contains('usable')) {
                circle.classList.add('usable');
            }
            websocketCell.textContent = "waiting...";
        } else {
            if (circle.classList.contains('usable')) {
                circle.classList.replace('usable', 'using');
            }
            if (!circle.classList.contains('using')) {
                circle.classList.add('using');
            }
            websocketCell.textContent = sessionList[itemNum].webSocketId;
        }
        statusCell.appendChild(circle); // 원형 div를 td에 추가

        // <tr> 요소에 <td> 요소 추가
        row.appendChild(statusCell);
        row.appendChild(websocketCell);

        // <tr> 요소를 테이블에 추가
        tableBody.appendChild(row);
    }

    setTimeout(getSessionList, 1000) // 1초 후 재실행

}