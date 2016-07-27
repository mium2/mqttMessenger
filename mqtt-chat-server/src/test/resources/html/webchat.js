/**
 * Created by mium2 on 16. 7. 20..
 */
var ws;
var lastMessageSent;
var clientID = "TEST01";
var chatRoomUserCnt = 10;
var chatRoomID = "365bfa203943f4192c302143bbf5f87f";
var savedMsgArr = new Array();

if ("WebSocket" in window){
    // Let us open a web socket
    ws = new WebSocket("ws://localhost:8080/webchat");
    ws.binaryType = "arraybuffer";
    ws.onopen = function() {
        var connectMsg="CONNECT|"+clientID;
        ws.send(connectMsg);
    };

    ws.onmessage = function (evt){
        if(evt.data instanceof ArrayBuffer){
            console.log(evt.data);
        }else{
            var received_msg = evt.data;

            console.log("## [RECEIVED MSG]:"+received_msg);

            if(received_msg!="pong"){
                var revMsgArr = received_msg.split("|");
                var command = revMsgArr[0];
                if(command=="CONACK"){
                    var resultCode = revMsgArr[1];
                    var resultMsg = revMsgArr[2];
                    console.log("## [CONNECT ACK MSG]:("+resultCode+")"+resultMsg);
                    if(resultCode=="200"){
                        sendPing();
                    }else{
                        alert(resultMsg);
                    }
                }else if(command=="PUBACK"){
                    var topic = revMsgArr[1];
                    var messageid = revMsgArr[2];
                    // messageid를 이용하여 정상발송 완료 처리 하는 로직 구현.
                }else if(command=="PUBLISH"){
                    // 받았을 때는 발행자아이디를 받는다. 해당
                    var publisherid = revMsgArr[1];
                    var messageid = revMsgArr[2];
                    var topic = revMsgArr[3];
                    var payload = revMsgArr[4];
                    ws.send("PUBACK|"+publisherid+"|"+messageid);
                    putRevMsgUI(payload,"Y");
                }else if(command=="#SYS_MSG00"){
                    var topic = revMsgArr[1];
                    var messageid = revMsgArr[2];
                    var notRevCnt = revMsgArr[3];
                    var notReadDiv = document.getElementById(messageid+"_NotReadDiv");
                    if(notReadDiv==null)
                    return;
                    if(notRevCnt=="0"){
                        notRevCnt = "";
                    }
                    notReadDiv.innerText=notRevCnt;
                    console.log("## [REVINFO MSG] messageid:"+messageid+"  topic:"+topic+"  notReceiveCnt:"+notRevCnt);
                }else if(command=="#SYS_MSG04"){
                    //#SYS_MSG04|png|http://localhost:18080/download_file/images/1469172464572.png|http://localhost:18080/download_file/images/thumb/1469172464572.png
                    var publisherid = revMsgArr[1];
                    var messageid = revMsgArr[2];
                    var fileExt = revMsgArr[3];
                    var downloadUrl = revMsgArr[4];
                    var thumnailUrl = revMsgArr[5];
                    console.log("## [downloadUrl]:"+downloadUrl+"  thumnailUrl:"+thumnailUrl+"  fileExt:"+fileExt);
                    ws.send("PUBACK|"+publisherid+"|"+messageid);
                    putRevImgUI(downloadUrl,thumnailUrl,"Y");
                }
            }
        }

    };

    ws.onclose = function(){
        // websocket is closed.
        ws.send("close");
    };
}else{
    // The browser doesn't support WebSocket
    alert("WebSocket NOT supported by your Browser!");
}

//파일을 ArrayBuffer를 서버로 전송함
function sendFileArrayBuffer() {
    var file = document.querySelector('input[type="file"]').files[0];
    var fileObj = document.getElementById("attach");
    if(fileObj!=""){
        var pathHeader = fileObj.value.lastIndexOf("\\");
        console.log("## pathHeader : "+pathHeader);
        var pathMiddle = fileObj.value.lastIndexOf(".");
        var pathEnd = fileObj.value.length;
        var fileName = fileObj.value.substring(pathHeader+1, pathMiddle);
        var extName = fileObj.value.substring(pathMiddle+1, pathEnd);
        var allFilename = fileName+"."+extName;
    }

    var fileReader = new FileReader();
    fileReader.onload = function() {
        var fileArrayBuf = this.result;
        ///////////// 바이너리로 만들기 //////////////////////
        // Command 문자 바이너리만들기//
        var commandbuf = new ArrayBuffer(UTF8Length("#SYS_MSG03"));
        var commandBytes = new Uint8Array(commandbuf);
        stringToUTF8("#SYS_MSG03", commandBytes, 0);

        // 토픽명 바이너리만들기//
        var chatRoomIDbuf = new ArrayBuffer(UTF8Length(chatRoomID));
        var chatRoomIDBytes = new Uint8Array(chatRoomIDbuf);
        stringToUTF8(chatRoomID, chatRoomIDBytes, 0);

        // 토픽명길이 바이너리만들기//
        var chatRoomIDLenbuf = new ArrayBuffer(UTF8Length(chatRoomIDBytes.length+""));
        var chatRoomIDLenBytes = new Uint8Array(chatRoomIDLenbuf);
        stringToUTF8(chatRoomIDBytes.length+"", chatRoomIDLenBytes, 0);

        // 파일명 바이너리 만들기
        var fileNameBuf = new ArrayBuffer(UTF8Length(allFilename));
        var fileNameBytes = new Uint8Array(fileNameBuf);
        stringToUTF8(allFilename, fileNameBytes, 0);

        //파일명 길이 바이너리 만들기
        var fileNameLen = fileNameBytes.length+"";
        console.log("## 파일명 길이:"+fileNameLen);
        var fileNameLenbuf = new ArrayBuffer(UTF8Length(fileNameLen));
        var fileNameLenBytes = new Uint8Array(fileNameLenbuf);
        stringToUTF8(fileNameLen, fileNameLenBytes, 0);
        ///////////////////////////////////////////////////

        console.log("## filename :"+ allFilename);
        console.log("## fileNameBytes length:"+fileNameBytes.length);
        console.log(("## fileArrayBuf length:"+fileArrayBuf.byteLength));

        var offset = 0;
        // 보낼 총바이트 Array할당
        //Command(#SYS_MSG03)10byte+5byte(메세지아이디) +4byte(토픽길이)+토픽+ 파일명길이 4byte  + 파일명 가변  + 보낼파일바이트
        var sendBuff = new Uint8Array(commandBytes.length+5+4+chatRoomIDBytes.length+4+fileNameBytes.length+fileArrayBuf.byteLength);
        console.log("### sendBuff.length:"+sendBuff.length);

        // Command bytes 넣음
        sendBuff.set(commandBytes,offset);
        offset = commandBytes.length;
        console.log("## Command 넣은 후 offset :"+offset);

        // 메세지 아이디 넣음
        var makeMsgId = makeMessageID()+"";
        var msgIDbuf = new ArrayBuffer(UTF8Length(makeMsgId));
        var msgIDBytes = new Uint8Array(msgIDbuf);
        stringToUTF8(makeMsgId, msgIDBytes, 0);
        sendBuff.set(msgIDBytes,offset);
        offset = offset+5;
        console.log("## makeMsgId 넣은 후 offset :"+offset);

        // 토픽 길이넣음
        sendBuff.set(chatRoomIDLenBytes,offset);
        offset = offset+4;
        console.log("## chatRoomIDLenBytes 넣은 후 offset :"+offset);

        // 토픽 넣음
        sendBuff.set(chatRoomIDBytes,offset);
        offset = offset+chatRoomIDBytes.length;
        console.log("## chatRoomIDBytes 넣은 후 offset :"+offset);

        // 파일명 길이넣음
        sendBuff.set(fileNameLenBytes,offset);
        offset = offset+4;
        console.log("## 파일명 길이 넣은 후 : offset :"+offset);

        // 파일명 바이트 넣음
        sendBuff.set(fileNameBytes,offset);
        offset = offset+fileNameBytes.length;
        console.log("## 파일명 넣은 후 : offset :"+offset);

        // 보낼파일 바이너리 넣음
        sendBuff.set(new Uint8Array(fileArrayBuf),offset);

        putSendMsgUI("파일"+allFilename + " 전송",makeMsgId);

        ws.send(sendBuff);
        document.getElementById("message").value = "";
    };
    fileReader.readAsArrayBuffer(file);
}

function sendPing(){
    setInterval(
        function(){
            var interval = 120 * 1000;
            var currentTime = new Date().getTime();
            if( ! lastMessageSent || lastMessageSent < currentTime - interval ){
                ws.send("ping");
                lastMessageSent = currentTime;
            }
        },
        1000
    );
}

//메세지 발송
window.onload = function() {
    initLoad();

    document.getElementById("message").onkeypress = function() {
        if (event.keyCode == '13') {

            // Web Socket is connected, send data using send()
            var sendMsg = document.getElementById("message").value;
            ///////////// 바이너리로 만들기 //////////////////////
            //var buffer = new ArrayBuffer(UTF8Length(sendMsg));
            //var byteStream = new Uint8Array(buffer);
            //stringToUTF8(sendMsg, byteStream, 0);
            ///////////////////////////////////////////////////
            var makeMsgId = makeMessageID();
            putSendMsgUI(sendMsg,makeMsgId,"Y");

            // command|clientid|messageid|topic|메세지
            sendMsg="PUBLISH|"+clientID+"|"+makeMsgId+"|"+chatRoomID+"|"+sendMsg;

            ws.send(sendMsg);
            document.getElementById("message").value = "";
        }
    }
}

function initLoad(){
    //localStorage.removeItem(clientID+"_msg");
    var JsonSavedMsg = localStorage.getItem(clientID+"_msg");
    if(JsonSavedMsg!=null){
        console.log("### put saved msg: " + JsonSavedMsg);
        savedMsgArr = JSON.parse(JsonSavedMsg);
    }

    console.log("### savedMsgArr.length:"+savedMsgArr.length);
    for(var i=0; i<savedMsgArr.length; i++){
        var uiMsgArr = savedMsgArr[i].split("|");
        if(uiMsgArr[0]=="0"){
            putSendMsgUI(uiMsgArr[1],uiMsgArr[2],"N");
        }else if(uiMsgArr[0]=="1"){
            putRevMsgUI(uiMsgArr[1],"N")
        }else if(uiMsgArr[0]=="2"){
            putRevImgUI(uiMsgArr[1],uiMsgArr[2],"N")
        }
    }
}


function putSendMsgUI(sendMsg, makeMsgId, isSave_YN){
    if(isSave_YN=="N"){
        chatRoomUserCnt = "";
    }
    var now = new Date();
    var selfChatDiv = "<li class='self'>";
    selfChatDiv=selfChatDiv+"<div class='avatar'><img src='http://i.imgur.com/DY6gND0.png' draggable='false'/></div>";
    selfChatDiv=selfChatDiv+"<div class='msg'>";
    selfChatDiv=selfChatDiv+"<p>"+sendMsg+"</p>";
    selfChatDiv=selfChatDiv+"<div id='"+makeMsgId+"_NotReadDiv' style='font-size: 0.7rem;color: darkcyan'>"+chatRoomUserCnt+"</div><time>"+now.getHours()+":"+now.getMinutes()+"</time>";
    selfChatDiv=selfChatDiv+"</div></li>";

    var chatOl = document.getElementById("chat_ol");
    chatOl.insertAdjacentHTML('beforeend', selfChatDiv);

    if(isSave_YN=="Y"){
        if(savedMsgArr.length>=10){
            savedMsgArr.splice(0,1);
        }
        console.log("###1. savedMsgArr length :"+savedMsgArr.length);
        savedMsgArr.push("0|"+sendMsg+"|"+makeMsgId);
        console.log("###2. savedMsgArr length :"+savedMsgArr.length);

        var jsonString = JSON.stringify(savedMsgArr);
        //var putString = jsonString.substring(1,jsonString.length-1);
        console.log("###3. putString :"+jsonString);
        localStorage.setItem(clientID+"_msg",jsonString);
    }
}

function putRevMsgUI(revMsg, isSave_YN){
    var now = new Date();
    var otherChatDiv = "<li class='other'>";
    otherChatDiv=otherChatDiv+"<div class='avatar'><img src='http://i.imgur.com/HYcn9xO.png' draggable='false'/></div>";
    otherChatDiv=otherChatDiv+"<div class='msg'>";
    otherChatDiv=otherChatDiv+"<p>"+revMsg+"</p>";
    //otherChatDiv=otherChatDiv+"<p>테스트 이모티콘 <emoji class='books'/></p>";
    otherChatDiv=otherChatDiv+"<time>"+now.getHours()+":"+now.getMinutes()+"</time>";
    otherChatDiv=otherChatDiv+"</div></li>";

    var chatOl = document.getElementById("chat_ol");
    chatOl.insertAdjacentHTML('beforeend', otherChatDiv);

    if(isSave_YN=="Y"){
        if(savedMsgArr.length>=10){
            savedMsgArr.splice(0,1);
        }
        savedMsgArr.push("1|"+revMsg+"| ");
        var jsonString = JSON.stringify(savedMsgArr);
        localStorage.setItem(clientID+"_msg",jsonString);
    }
}

function putRevImgUI(downUrl, thumnailUrl,isSave_YN){
    console.log("### downUrl:"+downUrl+"   thumnailUrl:"+thumnailUrl);
    var now = new Date();
    var otherChatDiv = "<li class='other'>";
    otherChatDiv=otherChatDiv+"<div class='avatar'><img src='http://i.imgur.com/HYcn9xO.png' draggable='false'/></div>";
    otherChatDiv=otherChatDiv+"<div class='msg'>";
    otherChatDiv=otherChatDiv+"<p><a href='"+downUrl+"' target='_blank'><img src='" + thumnailUrl + "'></a></p>";
    //otherChatDiv=otherChatDiv+"<p>테스트 이모티콘 <emoji class='books'/></p>";
    otherChatDiv=otherChatDiv+"<time>"+now.getHours()+":"+now.getMinutes()+"</time>";
    otherChatDiv=otherChatDiv+"</div></li>";

    var chatOl = document.getElementById("chat_ol");
    chatOl.insertAdjacentHTML('beforeend', otherChatDiv);

    if(isSave_YN=="Y"){
        if(savedMsgArr.length>=10){
            savedMsgArr.splice(0,1);
        }
        savedMsgArr.push("3|"+downUrl+"|"+thumnailUrl);
        var jsonString = JSON.stringify(savedMsgArr);
        localStorage.setItem(clientID+"_msg",jsonString);
    }
}
