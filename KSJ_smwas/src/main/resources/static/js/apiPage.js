

let apiEndpoint;
let tr_id;
let tr_code;
let selectedShcodes = [];
let socket;
let websocketKey;
let isSelectAll = false; // 체크박스 전체선택/해제 여부 추적
let isHoCheFlag = false; // false : 체결(H0STCNT0), true : 호가(H0STASP0)   
// 종목 리스트 가져오기
function getJmList() {
    fetch("/jmCodeMaster.json")
        .then(response => response.json())
        .then(data => {
            const jmList = document.getElementById('jmList');

            // 테이블 생성
            const table = document.createElement('table');
            table.className = 'jm-table'; // css용 클래스명 추가

            // 테이블 바디 생성
            const tbody = document.createElement('tbody');

            data.forEach(stock => {
                const row = document.createElement('tr'); // 테이블 로우 생성

                const checkCell = document.createElement('td'); // 체크박스 셀
                const jmCell = document.createElement('td'); // 종목명 셀

                const checkbox = document.createElement('input');
                checkbox.type = 'checkbox';
                checkbox.id = stock.mksc_shrn_iscd;
                checkbox.name = 'stock';
                checkbox.value = stock.hts_kor_isnm;

                checkbox.addEventListener('click', () => {
                    checkSelect(checkbox); // 전체 선택/해제 로직 검사
                    updateSelectedShcodes(); // 선택된 종목 코드 업데이트
                });

                const label = document.createElement('label');
                label.htmlFor = stock.mksc_shrn_iscd;
                label.textContent = stock.hts_kor_isnm;

                checkCell.appendChild(checkbox);
                jmCell.appendChild(label);
                row.appendChild(checkCell);
                row.appendChild(jmCell);
                tbody.appendChild(row); // 로우를 테이블 바디에 추가
            });

            table.appendChild(tbody); // 테이블 바디를 테이블에 추가
            jmList.appendChild(table); // 테이블을 jmList 요소에 추가
        })
        .catch(error => console.error('JSON 파일 로딩 에러', error));
}


// 전체선택 체크박스 이벤트 리스너
document.querySelector('input[name="selectAll"]').addEventListener("change",function(){
	const checkboxes = document.querySelectorAll('input[type="checkbox"][name="stock"]'); 	// 모든 'stock' 이름을 가진 체크박스를 선택
    checkboxes.forEach((checkbox) => {
        checkbox.checked = document.querySelector('input[name="selectAll"]').checked; 		// 체크박스 상태를 전체 선택 체크박스의 상태와 동일하게 설정
    });
    
    updateSelectedShcodes();
}) 



// 개별선택 체크박스 이벤트: 전체선택 되지 않은 경우에만 종목코드 저자
document.querySelectorAll('input[name="stock"]').forEach(checkbox => {
	checkbox.addEventListener('change', function() {
		if (!isSelectAll) {				
			updateSelectedShcodes();	
		}
	})
});


// 전체 선택 후 개별 체크 해제 시 전체 선택도 해제
function checkSelect(targetCheckbox) {
	const selectAllCheckbox = document.querySelector('input[name="selectAll"]');		// 전체 선택 체크박스 요소를 찾음
	if (!targetCheckbox.checked) { 														// 클릭된 체크박스가 해제된 경우
		selectAllCheckbox.checked = false; 												// 전체 선택 체크박스를 해제함
	} else {
		const checkboxes = document.querySelectorAll('input[name="stock"]'); 			// 전체 선택을 제외한 모든 체크박스 요소를 찾음
		const allChecked = Array.from(checkboxes).every(checkbox => checkbox.checked);  // 전체 선택을 제외한 모든 체크박스가 체크되었는지 확인
		selectAllCheckbox.checked = allChecked; 										// 개별 체크박스가 체크되었다면, 전체 선택 체크박스도 체크함
	}
}



// 체크 된 값 selectedShcodes 리스트에 추가
// 'skipAlert = false': 실시간 체결가 조회 시에는 특정 로직을 건너뛸 수 있도록 함
function updateSelectedShcodes(skipAlert = false) {
	selectedShcodes = [];
	const apiSelected = document.getElementById('apiSelect').value
	const checkboxes = document.querySelectorAll('input[name="stock"]:checked');
	const fieldToDisable = document.querySelectorAll('input[name="FID_COND_MRKT_DIV_CODE"], input[name="FID_INPUT_ISCD"]')

	checkboxes.forEach(checkbox => {
		selectedShcodes.push(checkbox.id);
	})

	console.log("selectedShcodes: " + selectedShcodes);

	const isSelectedJm = selectedShcodes.length > 0;


	// 실시간 체결가 조회 버튼 클릭 시 경고를 표시하지 않음
	if (!skipAlert) {
		
		// 종목을 선택했고 API 종류가 업종 기간별 시세일 경우
		if (apiSelected == "dailyIndexchartprice") {

			if (isSelectedJm) {
				document.querySelector('#submitBtn').disabled = true;	// 조회 버튼 비활성화
				alert("종목 선택 시 해당 API 조회가 불가능합니다.\n다른 API 종류를 선택하시거나 종목 체크 해제하여 다시 시도해주세요.")

			} else {
				document.querySelector('#submitBtn').disabled = false;

			}

		// 그 외인 경우
		} else {
			document.querySelector('#submitBtn').disabled = false;	// 조회 버튼 활성화

			// 선택된 종목이 있을 경우
			if (isSelectedJm) {
				fieldToDisable.forEach(field => {
					field.disabled = true;							// input 필드 비활성화
					field.placeholder = '선택 종목의 내용으로 자동 적용'	// 조건 시장 분류 코드, 종목코드 placeholder 변경
				})

				// 선택된 종목이 없을 경우
			} else {
				fieldToDisable.forEach(field => {
					field.disabled = false;							// input 필드 활성화
					if (field.name == "FID_COND_MRKT_DIV_CODE") {
						field.placeholder = '조건 시장 분류 코드'; 		// 원래 placeholder
					} else if (field.name == "FID_INPUT_ISCD") {
						field.placeholder = '입력 종목코드'; 			// 원래 placeholder
					}

				})
			}
		}
	}
}


// 선택된 API 종류에 따라 QueryParams 입력 테이블 다르게 보여주기
function updateInputFields() {
	const apiSelect = document.getElementById('apiSelect').value // 선택된 API
	const inputTable = document.getElementById('inputTable');
	let fieldsToUpdate;

	// response 창 초기화
	document.getElementById('response').innerHTML = "";
	
	// 실시간 체결가 조회 끊기
	if (socket) {
		socket.close();
	
	}
	
	// 테이블 초기화 (첫 번째 행(헤더) 제외)
	while (inputTable.rows.length > 1) {
		inputTable.deleteRow(1);
	}
	
	
	// 국내주식 업종 기간별 시세(일/주/월/년)일 경우의 입력 필드
	if (apiSelect == "dailyIndexchartprice") {
		const dailyIndexchartpriceField = [
			{ key: "FID_COND_MRKT_DIV_CODE", placeholder: "조건 시장 분류 코드", description: "U: 업종" },
			{ key: "FID_INPUT_ISCD", placeholder: "업종 상세코드", description: "0001: 종합, 0002: 대형주... 포탈(FAQ: 종목정보 다운로드 - 업종코드 참조)" },
			{ key: "FID_INPUT_DATE_1", placeholder: "조회 시작일자	", description: "조회 시작일자 (ex. 20220501)" },
			{ key: "FID_INPUT_DATE_2", placeholder: "조회 종료일자", description: "조회 종료일자 (ex. 20220530)" },
			{ key: "FID_PERIOD_DIV_CODE", placeholder: "기간분류코드", description: "D:일봉, W:주봉, M:월봉, Y:년봉" },
		]; 
		
		fieldsToUpdate = dailyIndexchartpriceField;

	// 그 외의 입력 필드		
	} else {
		// 공통 입력 필드
		const commonFields = [
			{ key: "FID_COND_MRKT_DIV_CODE", placeholder: "조건 시장 분류 코드", description: "J : 주식, ETF, ETN" },
			{ key: "FID_INPUT_ISCD", placeholder: "입력 종목코드", description: "종목번호 (6자리) ETN의 경우, Q로 시작 (예: Q500001)" }
		];

		//  API별 추가 입력 필드
		const addFields = {
			'timeItemconclusion': [
				{ key: "FID_INPUT_HOUR_1", placeholder: "조회 시작시간", description: "기준시간 (6자리; HHMMSS) 예: 155000은 15시 50분 00초를 의미" }
			],
			'dailyPrice': [
				{ key: "FID_PERIOD_DIV_CODE", placeholder: "기간 분류 코드", description: "D" },
				{ key: "FID_ORG_ADJ_PRC", placeholder: "수정주가 원주가 가격 여부", description: "0: 수정주가 반영, 1: 수정주가 미반영" }
			],
			'dailyItemchartprice': [
				{ key: "FID_INPUT_DATE_1", placeholder: "입력 날짜 (시작)", description: "조회 시작일자 (ex. 20220501)" },
				{ key: "FID_INPUT_DATE_2", placeholder: "입력 날짜 (종료)", description: "조회 종료일자 (ex. 20220530)" },
				{ key: "FID_PERIOD_DIV_CODE", placeholder: "기간분류코드", description: "D:일봉, W:주봉, M:월봉, Y:년봉" },
				{ key: "FID_ORG_ADJ_PRC", placeholder: "수정주가 원주가 가격 여부", description: "0: 수정주가 반영, 1: 수정주가 미반영" }
			],
		}
		fieldsToUpdate = commonFields.concat(addFields[apiSelect] || []);	// 공통 입력 필드 + API별 주가 입력 필드 결합
	}

	// 테이블 행, 셀 속성 설정
	fieldsToUpdate.forEach(field => {
		const row = inputTable.insertRow();
		const cellKey = row.insertCell(0);
		const cellValue = row.insertCell(1);
		const cellDesc = row.insertCell(2);

		cellKey.textContent = field.key;				// cellkey에 입력된 텍스트 값을 key 값으로 설정

		const input = document.createElement('input');	// 입력 필드 생성
		input.type = 'text';

		input.name = field.key;							// 입력 필드 name = key
		input.placeholder = field.placeholder;
		cellValue.appendChild(input);					// 생성 된 입력 필드를 cellValue에 추가
		cellDesc.textContent = field.description;

	});
	
	// API 종류 변경하더라도 기존 종목 코드 그대로 가져감
	updateSelectedShcodes();
}


// INPUT 테이블 생성
function createInputField(id, placeholder = '') {

	const tr = document.createElement('tr');		// 테이블 행(tr) 요소 생성

	// 테이블 데이터(td) 요소 생성 - 입력 필드용
	const tdInput = document.createElement('td');
	const input = document.createElement('input');
	input.type = 'text';
	input.id = id;
	input.name = id;
	input.placeholder = placeholder; 				// placeholder 값 설정
	tdInput.appendChild(input);

	tr.appendChild(tdInput);						// 행(tr)에 입력 필드용 td 추가

	return tr;										// 완성된 행(tr) 반환
}


// 페이지 로드되자 마자 이벤트 실행
document.addEventListener('DOMContentLoaded', function() {

	// API 종류 선택 시 테이블 업데이트
	document.getElementById('apiSelect').addEventListener('change', updateInputFields);

	// formData 제출
	document.getElementById('apiForm').addEventListener('submit', function(event) {
		event.preventDefault(); 			// 새로고침 동작 방지
		updateSelectedShcodes();    		// 체크박스에서 선택된 종목 코드를 배열로 수집
		inputTextToUpperCase();
		
		if (!checkBeforeSubmit()) {			// 입력값 체크 함수에서 false 반환된 경우
			event.preventDefault(); 		// 폼 제출 동작 방지
		} else {		
			setApi();						// API 호출 준비
		}

	});

	
	// 종목 리스트 가져오기
	getJmList();			
	

	// 웹소켓 연결 및 키 발급
	setWebSocket();								
	
	// '실시간 체결가 조회' 버튼
	document.getElementById('rushTest').addEventListener('click', function(event) {
		event.preventDefault(); 				// 새로고침 동작 방지
		updateSelectedShcodes(true);    		// 체크박스에서 선택된 종목 코드를 배열로 수집, 실시간 체결가 조회 시 API 종류 상관 없이 경고를 표시하지 않음
		isHoCheFlag = false;
		setRealApi();
	});
	// '실시간 호가 조회' 버튼
	document.getElementById('rushTest_2').addEventListener('click', function(event) {
		event.preventDefault(); 				// 새로고침 동작 방지
		updateSelectedShcodes(true);    		// 체크박스에서 선택된 종목 코드를 배열로 수집, 실시간 체결가 조회 시 API 종류 상관 없이 경고를 표시하지 않음
		isHoCheFlag = true;
		setRealApi();
	});
	
	
	// '웹소켓 끊기' 버튼
	document.getElementById('rushTestEnd').addEventListener('click', function(event) {
		event.preventDefault(); 	
		
		if(socket) {
			socket.close();
			console.log("웹소켓 연결 종료")
			document.getElementById('response').innerHTML = "";

		}
		
		setWebSocket();	// 실시간 데이터 조회 종료 후 웹소켓 연결 및 키 발급
	});	
});

	

// 웹소켓 연결, 수신
function setWebSocket() {
	socket = new WebSocket('ws://203.109.30.207:10001/connect');


	socket.onerror = function(error) {
		console.error("WebSocket 연결 실패: ", error);
		reject("WebSocket 연결에 실패했습니다. 네트워크 상태를 확인해주세요.");
	};

	socket.onopen = function() {
		console.log("WebSocket 연결 성공");
	};

	socket.onmessage = function(event) {
		const messageObj = JSON.parse(event.data);
	
		// 웹소켓 키 값 받음
		if (messageObj.Data && messageObj.Data.websocketkey) {
			console.log("WebSocket 키 발급 성공: ", messageObj.Data.websocketkey);
			websocketKey = messageObj.Data.websocketkey

		// 데이터 수신 받았을 경우
		} else {
			// 데이터 받아서 그리기
			const messageObj = JSON.parse(event.data);
			console.log(messageObj)
			const formattedJson = JSON.stringify(messageObj, null, 4); 													// 가독성있게 변환
			const currentContent = document.getElementById('response').innerHTML; 										// 기존의 response 내용을 가져옴
			document.getElementById('response').innerHTML = currentContent + '<hr><pre>' + formattedJson + '</pre>';	// 기존 내용에 새 내용 붙여 씀


			// 데이터 적재하기
			const apiUri = 'http://203.109.30.207:10001/stackingForRush';
			
			fetch(apiUri, {
				method: 'post',
				headers: {
					'Content-Type': 'application/json;charset=utf-8'
				},
				body: event.data
			})
			.catch(error => console.error('Error:', error));
		}
	}
}


// 입력된 조건 시장 분류코드, 기간 분류 코드 대문자화 (폼 제출 시)
function inputTextToUpperCase() {
	const condMrktDivCodeInput = document.querySelector('input[name="FID_COND_MRKT_DIV_CODE"]');
	const periodDivCodeInput = document.querySelector('input[name="FID_PERIOD_DIV_CODE"]');

	if (condMrktDivCodeInput) {
		condMrktDivCodeInput.value = condMrktDivCodeInput.value.toUpperCase();
	}
	if (periodDivCodeInput) {
		periodDivCodeInput.value = periodDivCodeInput.value.toUpperCase();
	}
}


// 폼 제출 전 입력 필드 빈 곳 있는지, 입력값이 조건에 맞는지 체크
function checkBeforeSubmit() {
    const apiSelected = document.getElementById('apiSelect').value;
    const textInputs = document.querySelectorAll('#apiForm input[type="text"]');
    let alertMsg = "";

    // API 종류 선택하지 않았을 경우 경고창
    if (apiSelected == "") {
		alert('API 종류를 선택한 후 조회를 눌러주세요.');
        return false;
    }

	// 선택된 종목이 있을 경우
	if (selectedShcodes.length > 0) {
		const isEmptyField = Array.from(textInputs).some(input => {
			// 'FID_COND_MRKT_DIV_CODE'와 'FID_INPUT_ISCD' 필드는 검사에서 제외, 사용자가 입력하는 값 아님
			if (input.name == 'FID_COND_MRKT_DIV_CODE' || input.name == 'FID_INPUT_ISCD') {
				return false;
			}
			// 나머지 필드가 비어있는지 확인
			return input.value.trim() == "";
		});

		if (isEmptyField) {
			alert('입력되지 않은 값이 있습니다. 다시 확인해주세요.');
			return false;
		}
		
	// 선택된 종목이 없어 모든 필드를 입력해야 할 경우
	} else {
		const isEmptyField = Array.from(textInputs).some(input => input.value.trim() == "");
		if (isEmptyField) {
			alert('입력되지 않은 값이 있습니다. 다시 확인해주세요.');
			return false;
		}
	}

    // 입력 필드 검사
    for (const input of textInputs) {
        const value = input.value.trim();
        
        // 조건 시장 분류 코드 검사
        if (apiSelected !== 'dailyIndexchartprice' && input.name == 'FID_COND_MRKT_DIV_CODE' && !['J'].includes(value) && ![''].includes(value)) {
			alertMsg += "'FID_COND_MRKT_DIV_CODE'의 값은 'J'로 입력해주세요.\n";		

		} else if (apiSelected == 'dailyIndexchartprice' && input.name == 'FID_COND_MRKT_DIV_CODE' && !['U'].includes(value)) {
			alertMsg += "'FID_COND_MRKT_DIV_CODE'의 값은 'U'로 입력해주세요.\n";	
		}
		
        // 날짜 검사
        if (input.name == 'FID_INPUT_DATE_1' || input.name == 'FID_INPUT_DATE_2') {
            if (!/^\d{8}$/.test(value)) {
                alertMsg += "'FID_INPUT_DATE_1', 'FID_INPUT_DATE_2'는 YYYYMMDD 형식의 8자리 숫자로 입력해주세요.\n";
            }
        }

        // 시간 필드 검사
        if (input.name == 'FID_INPUT_HOUR_1' && !/^\d{6}$/.test(value)) {
            alertMsg += "'FID_INPUT_HOUR_1'은 HHMMSS 형식의 6자리 숫자로 입력해주세요.\n";
        }

        // 기간 구분 코드 검사
        if (apiSelected == 'dailyPrice' && input.name == 'FID_PERIOD_DIV_CODE' && !['D'].includes(value)) {
			alertMsg += "'FID_PERIOD_DIV_CODE'의 값은 'D'로 입력해주세요.\n";			

		} else if ((apiSelected == 'dailyIndexchartprice' || apiSelected == 'dailyItemchartprice') && input.name === 'FID_PERIOD_DIV_CODE' && !['D', 'W', 'M', 'Y'].includes(value)) {
            alertMsg += "'FID_PERIOD_DIV_CODE'의 값은 'D', 'W', 'M', 'Y' 중 하나여야 합니다.\n";
        }

        // 수정주가 반영 여부 검사
        if (input.name === 'FID_ORG_ADJ_PRC' && !['0', '1'].includes(value)) {
            alertMsg += "'FID_ORG_ADJ_PRC'는 '0' 또는 '1'이어야 합니다.\n";
        }
    }

	if (apiSelected == 'dailyIndexchartprice' || apiSelected == 'dailyItemchartprice') {
		// FID_INPUT_DATE_1과 FID_INPUT_DATE_2 비교
		const date1 = document.querySelector('input[name="FID_INPUT_DATE_1"]').value.trim();
		const date2 = document.querySelector('input[name="FID_INPUT_DATE_2"]').value.trim();
		if (date1 > date2) {
			alertMsg += "'FID_INPUT_DATE_1'은 'FID_INPUT_DATE_2'보다 클 수 없습니다.\n";
		}
	}
    
  
    if (alertMsg.length > 0) {
        alert(alertMsg);
        return false;
    }
    return true; // 모든 검사 통과
}



// 선택된 API의 TrId,TrCode 가져오기
function setApi() {
	const apiSelect = document.getElementById('apiSelect');
	const apiConfig = getApiConfig(apiSelect.value);

	tr_id = apiConfig.tr_id;
	tr_code = apiConfig.tr_code;

	if (selectedShcodes.length > 0) {   // 선택된 체크박스가 있다면, 체크된 종목 코드를 사용하여 API 연속 호출
		callApiWithSelectedShcodes(selectedShcodes);
	} else {							// 아니라면 일반 API 호출
		callApiWithFormData();
	}
}




// 선택된 API의 TrId,TrCode 반환
function getApiConfig(apiValue) {
	const apiItem = {
		'ccnl': { tr_id: 'FHKST01010300', tr_code: '/uapi/domestic-stock/v1/quotations/inquire-ccnl' },
		'dailyPrice': { tr_id: 'FHKST01010400', tr_code: '/uapi/domestic-stock/v1/quotations/inquire-daily-price' },
		'askingPriceExpCcn': { tr_id: 'FHKST01010200', tr_code: '/uapi/domestic-stock/v1/quotations/inquire-asking-price-exp-ccn' },
		'dailyIndexchartprice': { tr_id: 'FHKUP03500100', tr_code: '/uapi/domestic-stock/v1/quotations/inquire-daily-indexchartprice' },
		'dailyItemchartprice': { tr_id: 'FHKST03010100', tr_code: '/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice' },
		'investor': { tr_id: 'FHKST01010900', tr_code: '/uapi/domestic-stock/v1/quotations/inquire-investor' },
		'member': { tr_id: 'FHKST01010600', tr_code: '/uapi/domestic-stock/v1/quotations/inquire-member' },
		'price': { tr_id: 'FHKST01010100', tr_code: '/uapi/domestic-stock/v1/quotations/inquire-price' },
		'timeItemconclusion': { tr_id: 'FHPST01060000', tr_code: '/uapi/domestic-stock/v1/quotations/inquire-time-itemconclusion' }
	};
	return apiItem[apiValue] || {};

}


// 일반 API 호출
function callApiWithFormData() {
	const formData = new FormData(document.getElementById('apiForm'));
	const apiUri = 'http://203.109.30.207:10001/stacking';
	const body = getBodyByTrCode(tr_code, formData);

	let jsonData = {
		trCode: tr_code,
		rqName: '',
		header: { "tr_id": tr_id },
		objCommInput: body
	}

	console.log(jsonData);

	fetch(apiUri, {
		method: 'post',

		headers: {
			'Content-Type': 'application/json;charset=utf-8'
		},
		body: JSON.stringify(jsonData)

	})
	.then(response => {
		if (!response.ok) {
			// 응답 상태가 성공적이지 않은 경우 오류 처리
			throw new Error('네트워크 응답이 올바르지 않습니다.');
		}
		return response.text(); // 응답을 텍스트로 변환
	})
	.then(text => {
		try {
			const data = JSON.parse(text); // 텍스트를 JSON으로 파싱
			console.log(data);
			// 응답 데이터를 HTML에 출력
			document.getElementById('response').innerHTML = '<pre>' + JSON.stringify(data, null, 4) + '</pre>';
		} catch (error) {
			console.error('JSON 파싱 오류:', error);
			// JSON 파싱 오류 처리
			document.getElementById('response').innerHTML = 'JSON 파싱 오류';
			console.log(response)
		}
	})
	.catch(error => {
		// 네트워크 오류 처리
		console.error('오류:', error);
	});
}


// 로딩 화면 시작
function loadingStart() {
	const loading = document.querySelector('#loading');
	loading.style.display = 'block';
}


// 로딩 화면 종료
function loadingEnd() {
	const loading = document.querySelector('#loading');
	loading.style.display = 'none';
}


// callApiWithCheckFormData 반복 호출
async function callApiWithSelectedShcodes(selectedShcodes) {
	let results = [];
	
	loadingStart(); // 로딩 화면 시작

    for (const shcode of selectedShcodes) {
        const checkFormData = new FormData();
        
		// 국내 주식 업종기간별 시세(일/주/월/년) 외 조건 시장 분류 코드 = 'J'
        if (tr_id !== "FHKUP03500100") {
			checkFormData.append('FID_COND_MRKT_DIV_CODE', 'J');
		}         
        
        // 종목코드는 리스트에서 선택한 종목
        checkFormData.append('FID_INPUT_ISCD', shcode);
        
        const result = await callApiWithCheckFormData(checkFormData);
        results.push(result);
        
        // 초당 거래 건수 제한으로 인한 타임아웃 설정: 건당 0.2초
        await new Promise(resolve => setTimeout(resolve, 200));
    }
    
    loadingEnd(); // 로딩 화면 종료
   
    displayResults(results);
}



// 종목 리스트 선택 후 API 호출
async function callApiWithCheckFormData(checkFormData) {
	const marketDivCode = checkFormData.get('FID_COND_MRKT_DIV_CODE');
	const iscd = checkFormData.get('FID_INPUT_ISCD');
	const apiUri = 'http://203.109.30.207:10001/stacking';

	const formData = new FormData(document.getElementById('apiForm'));
	
	let jsonData = {}
	
	console.log(marketDivCode);
	

	// 선택한 API에 따라 JSON 데이터 다르게 설정 
	if (tr_id == "FHPST01060000") {
		jsonData = {
			trCode: tr_code,
			rqName: '',
			header: { "tr_id": tr_id },
			objCommInput: { "FID_COND_MRKT_DIV_CODE": marketDivCode, "FID_INPUT_ISCD": iscd,
							"FID_INPUT_HOUR_1": formData.get('FID_INPUT_HOUR_1')}
		}					
	} else if (tr_id == "FHKST01010400") {
		jsonData = {
			trCode: tr_code,
			rqName: '',
			header: { "tr_id": tr_id },
			objCommInput: { "FID_COND_MRKT_DIV_CODE": marketDivCode, "FID_INPUT_ISCD": iscd,
							"FID_PERIOD_DIV_CODE": formData.get('FID_PERIOD_DIV_CODE'), "FID_ORG_ADJ_PRC": formData.get('FID_ORG_ADJ_PRC')}
		}
		
	} else if (tr_id == "FHKUP03500100") {
		jsonData = {
			trCode: tr_code,
			rqName: '',
			header: { "tr_id": tr_id },
			objCommInput: { "FID_COND_MRKT_DIV_CODE": marketDivCode, "FID_INPUT_ISCD": iscd,
							"FID_INPUT_DATE_1": formData.get('FID_INPUT_DATE_1'), "FID_INPUT_DATE_2": formData.get('FID_INPUT_DATE_2'), "FID_PERIOD_DIV_CODE": formData.get('FID_PERIOD_DIV_CODE')}
		}
		
	} else if (tr_id == "FHKST03010100") {
		jsonData = {
			trCode: tr_code,
			rqName: '',
			header: { "tr_id": tr_id },
			objCommInput: { "FID_COND_MRKT_DIV_CODE": marketDivCode, "FID_INPUT_ISCD": iscd,
							"FID_INPUT_DATE_1": formData.get('FID_INPUT_DATE_1'), "FID_INPUT_DATE_2": formData.get('FID_INPUT_DATE_2'), 
							"FID_PERIOD_DIV_CODE": formData.get('FID_PERIOD_DIV_CODE'), "FID_ORG_ADJ_PRC": formData.get('FID_ORG_ADJ_PRC')}
		}

	} else if(tr_id == "FHKST01010300" || "FHKST01010200" || "FHKST01010900" || "FHKST01010600" || "FHKST01010100" )
		jsonData = {
			trCode: tr_code,
			rqName: '',
			header: { "tr_id": tr_id },
			objCommInput: { "FID_COND_MRKT_DIV_CODE": marketDivCode, "FID_INPUT_ISCD": iscd }
	}


	try {
		let response = await fetch(apiUri, {
			method: 'post',

			headers: {
				'Content-Type': 'application/json;charset=utf-8'
			},
			body: JSON.stringify(jsonData)

		})

		if (!response.ok) {
			throw new Error('네트워크 응답이 올바르지 않습니다.');
		}

		const text = await response.text();

		try {
			const data = JSON.parse(text);
			return { success: true, data: data };
		} catch (error) {
			return { success: false, error: 'JSON 파싱 오류' };
		}		
	}
	catch (error) {
		return { success: false, error: error.message };
	}
	
}

	
// 다중 조회 화면 표출 
function displayResults(results) {
	const responseElement = document.getElementById('response');
	const formattedJson = JSON.stringify(results, null, 4);
    responseElement.innerHTML = '<pre>' + formattedJson + '</pre>';
}



// 선택된 API에 따른 trId, formData 반환
function getBodyByTrCode(trCode, formData) {

	// 모든 API에 공통적으로 필요한 입력 필드
	const commonFields = {
		'FID_COND_MRKT_DIV_CODE': formData.get('FID_COND_MRKT_DIV_CODE'),
		'FID_INPUT_ISCD': formData.get('FID_INPUT_ISCD')
	}


	switch (trCode) {
		case '/uapi/domestic-stock/v1/quotations/inquire-ccnl':
		case '/uapi/domestic-stock/v1/quotations/inquire-asking-price-exp-ccn':
		case '/uapi/domestic-stock/v1/quotations/inquire-investor':
		case '/uapi/domestic-stock/v1/quotations/inquire-member':
		case '/uapi/domestic-stock/v1/quotations/inquire-price':
			return commonFields;


		case '/uapi/domestic-stock/v1/quotations/inquire-time-itemconclusion':
			return {
				...commonFields,
				'FID_INPUT_HOUR_1': formData.get('FID_INPUT_HOUR_1')
			}

		case '/uapi/domestic-stock/v1/quotations/inquire-daily-price':
			return {
				...commonFields,
				'FID_PERIOD_DIV_CODE': formData.get('FID_PERIOD_DIV_CODE'),
				'FID_ORG_ADJ_PRC': formData.get('FID_ORG_ADJ_PRC'),
			}

		case '/uapi/domestic-stock/v1/quotations/inquire-daily-indexchartprice':
			return {
				...commonFields,
				'FID_INPUT_DATE_1': formData.get('FID_INPUT_DATE_1'),
				'FID_INPUT_DATE_2': formData.get('FID_INPUT_DATE_2'),
				'FID_PERIOD_DIV_CODE': formData.get('FID_PERIOD_DIV_CODE'),
			}

		case '/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice':
			return {
				...commonFields,
				'FID_INPUT_DATE_1': formData.get('FID_INPUT_DATE_1'),
				'FID_INPUT_DATE_2': formData.get('FID_INPUT_DATE_2'),
				'FID_PERIOD_DIV_CODE': formData.get('FID_PERIOD_DIV_CODE'),
				'FID_ORG_ADJ_PRC': formData.get('FID_ORG_ADJ_PRC'),
			}
	}
}


// 실시간 체결가 조회 시 선택된 체크박스가 있다면, 체크된 종목 코드를 사용하여 API 연속 호출
function setRealApi() {
	if (selectedShcodes.length > 0) { 
		callRealApiWithSelectedShcodes(selectedShcodes);
	} else {							
		alert("종목 값을 선택한 후 조회해주세요.");
	}
}


// callRealApiWithCheckFormData 반복 호출
async function callRealApiWithSelectedShcodes(selectedShcodes) {
	console.log("실시간 조회 시작");
		
    for (const shcode of selectedShcodes) {
        const checkFormData = new FormData();
        checkFormData.append('FID_INPUT_ISCD', shcode);

        callRealApiWithCheckFormData(checkFormData, websocketKey);
    }  
}

	
// 실시간 데이터 조회 API
function callRealApiWithCheckFormData(checkFormData, websocketKey) {
	const iscd = checkFormData.get('FID_INPUT_ISCD');
	const apiUri = 'http://203.109.30.207:10001/requestReal';
	
	let jsonData = {
		trCode: "/tryitout/H0STCNT0",
		rqName: "",
		header: {
			"sessionKey": websocketKey, 	// onMessage로 수신한 웹소켓 키
			"tr_type": "1",
		},
		objCommInput: {
			"tr_id": "H0STCNT0",
			"tr_key": iscd		
		}
	}
	jsonData.objCommInput.tr_id = isHoCheFlag ? "H0STASP0": "H0STCNT0";
	jsonData.trCode= isHoCheFlag ? "/tryitout/H0STASP0": "/tryitout/H0STCNT0";

	fetch(apiUri, {
		method: 'post',
		headers: {
			'Content-Type': 'application/json;charset=utf-8'
		},
		body: JSON.stringify(jsonData)
	})
	.catch(error => console.error('Error:', error));

}

	