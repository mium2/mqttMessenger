/**
 * Created by mium2 on 16. 7. 20..
 */
var ws;
var lastMessageSent;
var clientID = "TEST01";
var chatRoomUserCnt = 1;
var chatRoomID = "867396357";

if ("WebSocket" in window)
{
    // Let us open a web socket
    ws = new WebSocket("ws://localhost:8080/webchat");
    ws.binaryType = "arraybuffer";
    ws.onopen = function()
    {
        var connectMsg="CONNECT|"+clientID;
        ws.send(connectMsg);
    };

    ws.onmessage = function (evt)
    {
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
                    putRevMsgUI(payload);
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
                    putRevImgUI(downloadUrl,thumnailUrl);
                }
            }
        }

    };

    ws.onclose = function()
    {
        // websocket is closed.
        ws.send("close");
    };
}else{
    // The browser doesn't support WebSocket
    alert("WebSocket NOT supported by your Browser!");
}

// Blob 를 파일에 저장
function saveData(blob, fileName) {
    var a = document.createElement("a");
    document.body.appendChild(a);
    a.style = "display: none";

    url = window.URL.createObjectURL(blob);
    a.href = url;
    a.download = fileName;
    a.click();
    window.URL.revokeObjectURL(url);
};

// ArrayBuffer 를 파일에 저장
function saveData2(arrayBuffer, fileName) {
    var a = document.createElement("a");
    document.body.appendChild(a);
    a.style = "display: none";
    var parts = [];
    parts.push(arrayBuffer);
    url = window.URL.createObjectURL(new Blob(parts));
    a.href = url;
    a.download = fileName;
    a.click();
    window.URL.revokeObjectURL(url);
};

//canvas의 이미지 데이터를 서버로 전송하는 예
function sendImgArrayBuffer(){
    // Sending canvas ImageData as ArrayBuffer
    var img = canvas_context.getImageData(0, 0, 400, 320);
    var binary = new Uint8Array(img.data.length);
    for (var i = 0; i < img.data.length; i++) {
        binary[i] = img.data[i];
    }
    ws.send(binary.buffer);
};

//파일을 Blob를 서버로 전송함
function sendFileBlob() {
    // Sending file as Blob
    var file = document.querySelector('input[type="file"]').files[0];
//            var receiver = $('#receiver').val();
//            var msg = {sender:clientId, receiver:receiver};
//            msg.fname = file.name;
//            //파일 데이터에 앞서 송,수신자, 파일명을 텍스트로 전송한다
//            ws.send(JSON.stringify(msg));
//            //파일 데이터를 전송한다
    ws.send(file);
}

//파일을 ArrayBuffer를 서버로 전송함
function sendFileArrayBuffer() {
    alert("sendFileArrayBuffer");
    var file = document.querySelector('input[type="file"]').files[0];
    var fileReader = new FileReader();
    fileReader.onload = function() {
        arrayBuffer = this.result;
//                console.log(arrayBuffer);
        ws.send(arrayBuffer);
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


/**
 * Takes a String and writes it into an array as UTF8 encoded bytes.
 * @private
 */
function stringToUTF8(input, output, start) {
    var pos = start;
    for (var i = 0; i<input.length; i++) {
        var charCode = input.charCodeAt(i);

        // Check for a surrogate pair.
        if (0xD800 <= charCode && charCode <= 0xDBFF) {
            var lowCharCode = input.charCodeAt(++i);
            if (isNaN(lowCharCode)) {
                throw new Error(format(ERROR.MALFORMED_UNICODE, [charCode, lowCharCode]));
            }
            charCode = ((charCode - 0xD800)<<10) + (lowCharCode - 0xDC00) + 0x10000;

        }

        if (charCode <= 0x7F) {
            output[pos++] = charCode;
        } else if (charCode <= 0x7FF) {
            output[pos++] = charCode>>6  & 0x1F | 0xC0;
            output[pos++] = charCode     & 0x3F | 0x80;
        } else if (charCode <= 0xFFFF) {
            output[pos++] = charCode>>12 & 0x0F | 0xE0;
            output[pos++] = charCode>>6  & 0x3F | 0x80;
            output[pos++] = charCode     & 0x3F | 0x80;
        } else {
            output[pos++] = charCode>>18 & 0x07 | 0xF0;
            output[pos++] = charCode>>12 & 0x3F | 0x80;
            output[pos++] = charCode>>6  & 0x3F | 0x80;
            output[pos++] = charCode     & 0x3F | 0x80;
        };
    }
    return output;
}

/**
 * Takes a String and calculates its length in bytes when encoded in UTF8.
 * @private
 */
function UTF8Length(input) {
    var output = 0;
    for (var i = 0; i<input.length; i++)
    {
        var charCode = input.charCodeAt(i);
        if (charCode > 0x7FF)
        {
            // Surrogate pair means its a 4 byte character
            if (0xD800 <= charCode && charCode <= 0xDBFF)
            {
                i++;
                output++;
            }
            output +=3;
        }
        else if (charCode > 0x7F)
            output +=2;
        else
            output++;
    }
    return output;
}

function parseUTF8(input, offset, length) {
    var output = "";
    var utf16;
    var pos = offset;

    while (pos < offset+length)
    {
        var byte1 = input[pos++];
        if (byte1 < 128)
            utf16 = byte1;
        else
        {
            var byte2 = input[pos++]-128;
            if (byte2 < 0)
                throw new Error(format(ERROR.MALFORMED_UTF, [byte1.toString(16), byte2.toString(16),""]));
            if (byte1 < 0xE0)             // 2 byte character
                utf16 = 64*(byte1-0xC0) + byte2;
            else
            {
                var byte3 = input[pos++]-128;
                if (byte3 < 0)
                    throw new Error(format(ERROR.MALFORMED_UTF, [byte1.toString(16), byte2.toString(16), byte3.toString(16)]));
                if (byte1 < 0xF0)        // 3 byte character
                    utf16 = 4096*(byte1-0xE0) + 64*byte2 + byte3;
                else
                {
                    var byte4 = input[pos++]-128;
                    if (byte4 < 0)
                        throw new Error(format(ERROR.MALFORMED_UTF, [byte1.toString(16), byte2.toString(16), byte3.toString(16), byte4.toString(16)]));
                    if (byte1 < 0xF8)        // 4 byte character
                        utf16 = 262144*(byte1-0xF0) + 4096*byte2 + 64*byte3 + byte4;
                    else                     // longer encodings are not supported
                        throw new Error(format(ERROR.MALFORMED_UTF, [byte1.toString(16), byte2.toString(16), byte3.toString(16), byte4.toString(16)]));
                }
            }
        }

        if (utf16 > 0xFFFF)   // 4 byte character - express as a surrogate pair
        {
            utf16 -= 0x10000;
            output += String.fromCharCode(0xD800 + (utf16 >> 10)); // lead character
            utf16 = 0xDC00 + (utf16 & 0x3FF);  // trail character
        }
        output += String.fromCharCode(utf16);
    }
    return output;
}

//메세지 발송
window.onload = function() {
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
            var now = new Date();
            var selfChatDiv = "<li class='self'>";
            selfChatDiv=selfChatDiv+"<div class='avatar'><img src='http://i.imgur.com/DY6gND0.png' draggable='false'/></div>";
            selfChatDiv=selfChatDiv+"<div class='msg'>";
            selfChatDiv=selfChatDiv+"<p>"+sendMsg+"</p>";
            selfChatDiv=selfChatDiv+"<div id='"+makeMsgId+"_NotReadDiv' style='font-size: 0.7rem;color: darkcyan'>"+chatRoomUserCnt+"</div><time>"+now.getHours()+":"+now.getMinutes()+"</time>";
            selfChatDiv=selfChatDiv+"</div></li>";

            var chatOl = document.getElementById("chat_ol");
            chatOl.insertAdjacentHTML('beforeend', selfChatDiv);
            // command|clientid|messageid|topic|메세지
            sendMsg="PUBLISH|"+clientID+"|"+makeMsgId+"|"+chatRoomID+"|"+sendMsg;

            ws.send(sendMsg);
            document.getElementById("message").value = "";
        }
    }
}

function putRevMsgUI(revMsg){
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
}

function putRevImgUI(downUrl, thumnailUrl){
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
}

function makeMessageID(){
    var msgid = localStorage.getItem("messageid");
    if(msgid==null){
        msgid = 1;
        localStorage.setItem("messageid",msgid);
        return msgid;
    }else{
        if(msgid>=65536){
            msgid = 1;
        }else{
            msgid++;
        }
        localStorage.setItem("messageid",msgid);
        return msgid;
    }

}