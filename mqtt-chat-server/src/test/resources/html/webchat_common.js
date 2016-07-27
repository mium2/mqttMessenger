/**
 * Created by mium2 on 16. 7. 26..
 */
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

/**
 * Encodes an MQTT Multi-Byte Integer
 * @private
 */
function encodeMBI(number) {
    var output = new Array(1);
    var numBytes = 0;

    do {
        var digit = number % 128;
        number = number >> 7;
        if (number > 0) {
            digit |= 0x80;
        }
        output[numBytes++] = digit;
    } while ( (number > 0) && (numBytes<4) );

    return output;
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