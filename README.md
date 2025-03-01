### ChatGPT Assist 어플리케이션
https://openai.com ChatGPT API를 이용한 어플리케이션 입니다. 

자신의 API Key을 설정하여, Local에 저장한 후 API Request 할 때 사용하여 ChatGPT와 대화를 나눌 수 있습니다. 

**ChatGPT API 요금은 본인이 직접 결제 해야합니다**

---
### 핵심 기능
- **메신져와 같은 UI**
  - 채팅방 목록, 채팅방의 UI를 적용하여 메신저와 같은 UI를 제공합니다.
  - 버튼 하나만으로 손쉽게 ChatGPT 채팅방을 생성할 수 있습니다. 여러 대화의 흐름을 채팅방으로 관리 할 수 있습니다. 
  - 길어질 수 있는 ChatGPT API Response를 대응하여, Services(백그라운드)에서 Request 요청을 보냅니다.
  - 동시에 여러 채팅방에서 ChatGPT에게 질문을 할 수 있으며, 이는 부드러운 흐름으로 진행될 수 있도록 합니다.
    
- **ChatGPT API Token Maxium**
  - API Token Maxium 4096에 맞게 대화 내역을 GPT에게 전송하여 이어지는 대화를 ChatGPT와 나눌 수 있습니다.
  - API Token Maxium은 설정에서 1000 - 16000 사이로 설정 하여 Local에 저장합니다.
  - Maximum 값에 따라 [gpt3.5 turbo] or [gpt3.5 turbo-16k] 분기하여 사용 됩니다.
  - 만약 최대 값이 16000이고, 전송하고 반환 받는 token이 4096 이하이면 [gpt3.5 turbo]를 사용하고 그 이상이면 [gpt3.5 turbo-16k]를 사용합니다.
  - https://github.com/knuddelsgmbh/jtokkit 를 사용하여 구현 하였습니다.
  

- **백그라운드에서 ChatGPT에게 전송한 대화 내역을 ChatGPT를 통해 요약 합니다.**
  - 앱 내에서 On/Off로 설정 가능하며, 일정 Token 이상의 대화 내용을 ChatGPT를 이용하여 요약합니다.
  - 실제 사용자에게 보이는 Content는 요약 전 원문을 표시하며, API Request에는 요약 된 내용을 전달 합니다.
  - 이 기능을 활용 한다면 앞으로 보낼 Token 사용량을 절약할 수 있습니다.
 
- **API 이용 요금 제한을 설정할 수 있습니다.**
    - 앱 내에서 API 이용 요금 제한을 설정할 수 있습니다.
    - Total Token을 계산하여, OpenAI 요금 정책에 맞게 계산하여 처리합니다. 계산은 USD 기준으로 설정 가능 합니다.

---
### Android Skill & Architecture
- **Android Clean Architecture**
  - Presentation -> Domain <- Data 레이어 형태로 제작 되었으며, 각 레이어는 Module로 관리 됩니다.
 
- **design pattern MVP/VM WhatEver**
  - View와 ViewModel은 1:1 관계를 Default로 유지합니다.
  - 필요에 따라서 View와 ViewModel은 N:1 관계가 유지 될 수 있습니다. (ex:: Fragment) 

- **Local Lite SQL Room & Flow**
  - Room의 데이터를 Flow를 통해 UI에 전달할 수 있습니다.
  - UI는 Flow에서 최종적으로 발행되는 데이터에 따라 Update 됩니다.

- **Services**
  - Services를 이용하여, 독립적인 LifeCycle을 이용하여, 백그라운드에서 ChatGPT의 Reqeust 담당합니다.
  - 유저에게 보이는 화면과 상관 없이 서버의 Reqeust는 항시 유지 될 수 있습니다.
 
- **BuildSrc**
  - Kotlin-dsl를 사용하고 있으며 여러 모듈의 gradle를 BuildSrc를 통해 일관성 있게 관리하고 있습니다.
