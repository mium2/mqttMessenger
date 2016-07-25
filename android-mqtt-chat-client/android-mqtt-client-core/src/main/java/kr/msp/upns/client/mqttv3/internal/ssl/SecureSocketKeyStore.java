package kr.msp.upns.client.mqttv3.internal.ssl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.KeyStore;

/**
 * A bogus key store which provides all the required information to create an
 * example SSL connection.
 * 
 * To generate a bogus key store:
 * 
 * <pre>
 * keytool -genkey -alias securesocket \
 *        -keysize 2048 -validity 36500 \
 *        -keyalg RSA -dname "CN=securesocket" \
 *        -keypass inc0rrect -storepass mu$tch8ng3 \
 *        -keystore cert.jks
 * </pre>
 */
public class SecureSocketKeyStore {

	private static final byte[] CERT_BYTES = { (byte)254,(byte)237,(byte)254,(byte)237,(byte)0,(byte)0,(byte)0,(byte)2,(byte)0,
		(byte)0,(byte)0,(byte)2,(byte)0,(byte)0,(byte)0,(byte)2,(byte)0,(byte)9,(byte)109,
		(byte)113,(byte)116,(byte)116,(byte)45,(byte)99,(byte)104,(byte)97,(byte)116,(byte)0,(byte)0,
		(byte)1,(byte)85,(byte)226,(byte)235,(byte)249,(byte)61,(byte)0,(byte)5,(byte)88,(byte)46,
		(byte)53,(byte)48,(byte)57,(byte)0,(byte)0,(byte)2,(byte)38,(byte)48,(byte)130,(byte)2,
		(byte)34,(byte)48,(byte)130,(byte)1,(byte)139,(byte)160,(byte)3,(byte)2,(byte)1,(byte)2,
		(byte)2,(byte)9,(byte)0,(byte)208,(byte)124,(byte)192,(byte)206,(byte)108,(byte)135,(byte)160,
		(byte)231,(byte)48,(byte)13,(byte)6,(byte)9,(byte)42,(byte)134,(byte)72,(byte)134,(byte)247,
		(byte)13,(byte)1,(byte)1,(byte)5,(byte)5,(byte)0,(byte)48,(byte)83,(byte)49,(byte)11,
		(byte)48,(byte)9,(byte)6,(byte)3,(byte)85,(byte)4,(byte)6,(byte)19,(byte)2,(byte)75,
		(byte)82,(byte)49,(byte)14,(byte)48,(byte)12,(byte)6,(byte)3,(byte)85,(byte)4,(byte)7,
		(byte)19,(byte)5,(byte)83,(byte)101,(byte)111,(byte)117,(byte)108,(byte)49,(byte)16,(byte)48,
		(byte)14,(byte)6,(byte)3,(byte)85,(byte)4,(byte)10,(byte)19,(byte)7,(byte)67,(byte)111,
		(byte)109,(byte)112,(byte)97,(byte)110,(byte)121,(byte)49,(byte)18,(byte)48,(byte)16,(byte)6,
		(byte)3,(byte)85,(byte)4,(byte)11,(byte)19,(byte)9,(byte)84,(byte)101,(byte)115,(byte)116,
		(byte)32,(byte)84,(byte)101,(byte)97,(byte)109,(byte)49,(byte)14,(byte)48,(byte)12,(byte)6,
		(byte)3,(byte)85,(byte)4,(byte)3,(byte)19,(byte)5,(byte)109,(byte)105,(byte)117,(byte)109,
		(byte)50,(byte)48,(byte)30,(byte)23,(byte)13,(byte)49,(byte)54,(byte)48,(byte)55,(byte)49,
		(byte)51,(byte)48,(byte)54,(byte)49,(byte)55,(byte)53,(byte)57,(byte)90,(byte)23,(byte)13,
		(byte)50,(byte)54,(byte)48,(byte)55,(byte)49,(byte)49,(byte)48,(byte)54,(byte)49,(byte)55,
		(byte)53,(byte)57,(byte)90,(byte)48,(byte)83,(byte)49,(byte)11,(byte)48,(byte)9,(byte)6,
		(byte)3,(byte)85,(byte)4,(byte)6,(byte)19,(byte)2,(byte)75,(byte)82,(byte)49,(byte)14,
		(byte)48,(byte)12,(byte)6,(byte)3,(byte)85,(byte)4,(byte)7,(byte)19,(byte)5,(byte)83,
		(byte)101,(byte)111,(byte)117,(byte)108,(byte)49,(byte)16,(byte)48,(byte)14,(byte)6,(byte)3,
		(byte)85,(byte)4,(byte)10,(byte)19,(byte)7,(byte)67,(byte)111,(byte)109,(byte)112,(byte)97,
		(byte)110,(byte)121,(byte)49,(byte)18,(byte)48,(byte)16,(byte)6,(byte)3,(byte)85,(byte)4,
		(byte)11,(byte)19,(byte)9,(byte)84,(byte)101,(byte)115,(byte)116,(byte)32,(byte)84,(byte)101,
		(byte)97,(byte)109,(byte)49,(byte)14,(byte)48,(byte)12,(byte)6,(byte)3,(byte)85,(byte)4,
		(byte)3,(byte)19,(byte)5,(byte)109,(byte)105,(byte)117,(byte)109,(byte)50,(byte)48,(byte)129,
		(byte)159,(byte)48,(byte)13,(byte)6,(byte)9,(byte)42,(byte)134,(byte)72,(byte)134,(byte)247,
		(byte)13,(byte)1,(byte)1,(byte)1,(byte)5,(byte)0,(byte)3,(byte)129,(byte)141,(byte)0,
		(byte)48,(byte)129,(byte)137,(byte)2,(byte)129,(byte)129,(byte)0,(byte)143,(byte)32,(byte)243,
		(byte)148,(byte)134,(byte)227,(byte)25,(byte)215,(byte)248,(byte)245,(byte)207,(byte)236,(byte)53,
		(byte)65,(byte)56,(byte)112,(byte)99,(byte)255,(byte)141,(byte)175,(byte)243,(byte)23,(byte)78,
		(byte)140,(byte)30,(byte)69,(byte)13,(byte)23,(byte)242,(byte)156,(byte)23,(byte)5,(byte)196,
		(byte)203,(byte)139,(byte)34,(byte)42,(byte)164,(byte)141,(byte)213,(byte)14,(byte)192,(byte)85,
		(byte)69,(byte)170,(byte)6,(byte)127,(byte)44,(byte)112,(byte)42,(byte)111,(byte)167,(byte)124,
		(byte)125,(byte)79,(byte)153,(byte)206,(byte)218,(byte)137,(byte)217,(byte)235,(byte)244,(byte)249,
		(byte)133,(byte)190,(byte)116,(byte)72,(byte)5,(byte)231,(byte)112,(byte)1,(byte)82,(byte)115,
		(byte)165,(byte)62,(byte)6,(byte)175,(byte)113,(byte)132,(byte)87,(byte)73,(byte)185,(byte)21,
		(byte)26,(byte)85,(byte)165,(byte)176,(byte)14,(byte)77,(byte)151,(byte)132,(byte)211,(byte)106,
		(byte)251,(byte)182,(byte)142,(byte)112,(byte)167,(byte)196,(byte)105,(byte)234,(byte)180,(byte)253,
		(byte)100,(byte)56,(byte)0,(byte)134,(byte)236,(byte)223,(byte)39,(byte)140,(byte)182,(byte)9,
		(byte)3,(byte)91,(byte)43,(byte)176,(byte)0,(byte)17,(byte)183,(byte)209,(byte)194,(byte)119,
		(byte)199,(byte)22,(byte)85,(byte)225,(byte)213,(byte)2,(byte)3,(byte)1,(byte)0,(byte)1,
		(byte)48,(byte)13,(byte)6,(byte)9,(byte)42,(byte)134,(byte)72,(byte)134,(byte)247,(byte)13,
		(byte)1,(byte)1,(byte)5,(byte)5,(byte)0,(byte)3,(byte)129,(byte)129,(byte)0,(byte)117,
		(byte)67,(byte)93,(byte)234,(byte)103,(byte)137,(byte)126,(byte)85,(byte)222,(byte)161,(byte)2,
		(byte)198,(byte)219,(byte)199,(byte)211,(byte)123,(byte)117,(byte)253,(byte)170,(byte)28,(byte)174,
		(byte)241,(byte)131,(byte)100,(byte)244,(byte)214,(byte)232,(byte)218,(byte)15,(byte)202,(byte)184,
		(byte)144,(byte)199,(byte)125,(byte)175,(byte)135,(byte)60,(byte)211,(byte)52,(byte)159,(byte)22,
		(byte)155,(byte)157,(byte)225,(byte)20,(byte)139,(byte)250,(byte)170,(byte)62,(byte)150,(byte)160,
		(byte)32,(byte)140,(byte)48,(byte)192,(byte)31,(byte)168,(byte)82,(byte)17,(byte)143,(byte)135,
		(byte)242,(byte)235,(byte)53,(byte)90,(byte)10,(byte)127,(byte)254,(byte)129,(byte)189,(byte)243,
		(byte)137,(byte)118,(byte)32,(byte)235,(byte)23,(byte)80,(byte)207,(byte)187,(byte)124,(byte)79,
		(byte)89,(byte)177,(byte)208,(byte)200,(byte)104,(byte)87,(byte)28,(byte)206,(byte)30,(byte)13,
		(byte)190,(byte)220,(byte)41,(byte)190,(byte)180,(byte)40,(byte)240,(byte)46,(byte)19,(byte)184,
		(byte)169,(byte)113,(byte)45,(byte)119,(byte)82,(byte)168,(byte)30,(byte)191,(byte)74,(byte)120,
		(byte)215,(byte)235,(byte)118,(byte)210,(byte)98,(byte)134,(byte)228,(byte)62,(byte)189,(byte)219,
		(byte)190,(byte)219,(byte)199,(byte)216,(byte)156,(byte)28,(byte)55,(byte)0,(byte)0,(byte)0,
		(byte)1,(byte)0,(byte)8,(byte)109,(byte)115,(byte)112,(byte)45,(byte)99,(byte)104,(byte)97,
		(byte)116,(byte)0,(byte)0,(byte)1,(byte)85,(byte)226,(byte)233,(byte)82,(byte)170,(byte)0,
		(byte)0,(byte)1,(byte)143,(byte)48,(byte)130,(byte)1,(byte)139,(byte)48,(byte)14,(byte)6,
		(byte)10,(byte)43,(byte)6,(byte)1,(byte)4,(byte)1,(byte)42,(byte)2,(byte)17,(byte)1,
		(byte)1,(byte)5,(byte)0,(byte)4,(byte)130,(byte)1,(byte)119,(byte)125,(byte)215,(byte)2,
		(byte)24,(byte)41,(byte)234,(byte)6,(byte)232,(byte)148,(byte)157,(byte)226,(byte)131,(byte)79,
		(byte)68,(byte)224,(byte)240,(byte)73,(byte)219,(byte)231,(byte)182,(byte)143,(byte)74,(byte)205,
		(byte)10,(byte)215,(byte)27,(byte)252,(byte)146,(byte)217,(byte)238,(byte)59,(byte)18,(byte)170,
		(byte)77,(byte)151,(byte)89,(byte)10,(byte)16,(byte)144,(byte)121,(byte)209,(byte)25,(byte)83,
		(byte)99,(byte)103,(byte)199,(byte)202,(byte)116,(byte)74,(byte)23,(byte)249,(byte)187,(byte)255,
		(byte)215,(byte)137,(byte)25,(byte)13,(byte)6,(byte)140,(byte)76,(byte)79,(byte)194,(byte)62,
		(byte)31,(byte)193,(byte)35,(byte)13,(byte)179,(byte)76,(byte)41,(byte)3,(byte)35,(byte)137,
		(byte)135,(byte)188,(byte)159,(byte)138,(byte)231,(byte)64,(byte)3,(byte)100,(byte)120,(byte)94,
		(byte)164,(byte)251,(byte)186,(byte)10,(byte)48,(byte)74,(byte)30,(byte)203,(byte)77,(byte)72,
		(byte)29,(byte)171,(byte)120,(byte)66,(byte)39,(byte)233,(byte)117,(byte)196,(byte)90,(byte)220,
		(byte)137,(byte)102,(byte)135,(byte)108,(byte)219,(byte)91,(byte)70,(byte)89,(byte)159,(byte)217,
		(byte)110,(byte)15,(byte)186,(byte)117,(byte)74,(byte)94,(byte)223,(byte)227,(byte)0,(byte)27,
		(byte)235,(byte)75,(byte)116,(byte)20,(byte)74,(byte)1,(byte)182,(byte)21,(byte)238,(byte)14,
		(byte)118,(byte)67,(byte)193,(byte)90,(byte)214,(byte)52,(byte)127,(byte)188,(byte)154,(byte)5,
		(byte)187,(byte)154,(byte)69,(byte)118,(byte)41,(byte)53,(byte)40,(byte)108,(byte)238,(byte)52,
		(byte)198,(byte)90,(byte)208,(byte)248,(byte)77,(byte)157,(byte)189,(byte)198,(byte)116,(byte)235,
		(byte)19,(byte)88,(byte)47,(byte)78,(byte)142,(byte)242,(byte)124,(byte)0,(byte)117,(byte)105,
		(byte)74,(byte)252,(byte)246,(byte)67,(byte)12,(byte)241,(byte)221,(byte)50,(byte)196,(byte)110,
		(byte)64,(byte)15,(byte)148,(byte)146,(byte)116,(byte)13,(byte)194,(byte)49,(byte)3,(byte)84,
		(byte)10,(byte)45,(byte)254,(byte)189,(byte)105,(byte)47,(byte)83,(byte)58,(byte)6,(byte)217,
		(byte)103,(byte)126,(byte)237,(byte)95,(byte)140,(byte)151,(byte)222,(byte)69,(byte)203,(byte)40,
		(byte)205,(byte)12,(byte)237,(byte)28,(byte)223,(byte)14,(byte)185,(byte)151,(byte)57,(byte)20,
		(byte)10,(byte)175,(byte)39,(byte)128,(byte)119,(byte)44,(byte)187,(byte)24,(byte)168,(byte)47,
		(byte)236,(byte)119,(byte)162,(byte)96,(byte)158,(byte)202,(byte)92,(byte)152,(byte)131,(byte)82,
		(byte)242,(byte)38,(byte)158,(byte)99,(byte)247,(byte)163,(byte)139,(byte)166,(byte)245,(byte)146,
		(byte)6,(byte)130,(byte)112,(byte)93,(byte)18,(byte)64,(byte)140,(byte)225,(byte)204,(byte)254,
		(byte)198,(byte)2,(byte)107,(byte)113,(byte)193,(byte)252,(byte)6,(byte)2,(byte)28,(byte)70,
		(byte)50,(byte)0,(byte)197,(byte)193,(byte)230,(byte)88,(byte)254,(byte)213,(byte)77,(byte)244,
		(byte)73,(byte)93,(byte)47,(byte)130,(byte)242,(byte)105,(byte)168,(byte)193,(byte)145,(byte)83,
		(byte)206,(byte)0,(byte)96,(byte)251,(byte)190,(byte)195,(byte)6,(byte)255,(byte)78,(byte)227,
		(byte)129,(byte)224,(byte)156,(byte)178,(byte)206,(byte)20,(byte)56,(byte)44,(byte)59,(byte)194,
		(byte)127,(byte)255,(byte)198,(byte)220,(byte)135,(byte)57,(byte)46,(byte)148,(byte)148,(byte)152,
		(byte)123,(byte)219,(byte)185,(byte)39,(byte)191,(byte)73,(byte)157,(byte)235,(byte)208,(byte)40,
		(byte)79,(byte)124,(byte)191,(byte)127,(byte)235,(byte)143,(byte)200,(byte)188,(byte)198,(byte)201,
		(byte)28,(byte)49,(byte)80,(byte)79,(byte)116,(byte)29,(byte)142,(byte)96,(byte)136,(byte)54,
		(byte)141,(byte)134,(byte)182,(byte)107,(byte)92,(byte)248,(byte)180,(byte)36,(byte)44,(byte)23,
		(byte)92,(byte)44,(byte)179,(byte)140,(byte)102,(byte)10,(byte)185,(byte)104,(byte)24,(byte)86,
		(byte)138,(byte)33,(byte)0,(byte)0,(byte)0,(byte)1,(byte)0,(byte)5,(byte)88,(byte)46,
		(byte)53,(byte)48,(byte)57,(byte)0,(byte)0,(byte)3,(byte)43,(byte)48,(byte)130,(byte)3,
		(byte)39,(byte)48,(byte)130,(byte)2,(byte)229,(byte)160,(byte)3,(byte)2,(byte)1,(byte)2,
		(byte)2,(byte)4,(byte)117,(byte)69,(byte)209,(byte)19,(byte)48,(byte)11,(byte)6,(byte)7,
		(byte)42,(byte)134,(byte)72,(byte)206,(byte)56,(byte)4,(byte)3,(byte)5,(byte)0,(byte)48,
		(byte)101,(byte)49,(byte)11,(byte)48,(byte)9,(byte)6,(byte)3,(byte)85,(byte)4,(byte)6,
		(byte)19,(byte)2,(byte)107,(byte)114,(byte)49,(byte)16,(byte)48,(byte)14,(byte)6,(byte)3,
		(byte)85,(byte)4,(byte)8,(byte)19,(byte)7,(byte)85,(byte)110,(byte)107,(byte)110,(byte)111,
		(byte)119,(byte)110,(byte)49,(byte)16,(byte)48,(byte)14,(byte)6,(byte)3,(byte)85,(byte)4,
		(byte)7,(byte)19,(byte)7,(byte)85,(byte)110,(byte)107,(byte)110,(byte)111,(byte)119,(byte)110,
		(byte)49,(byte)16,(byte)48,(byte)14,(byte)6,(byte)3,(byte)85,(byte)4,(byte)10,(byte)19,
		(byte)7,(byte)85,(byte)110,(byte)107,(byte)110,(byte)111,(byte)119,(byte)110,(byte)49,(byte)16,
		(byte)48,(byte)14,(byte)6,(byte)3,(byte)85,(byte)4,(byte)11,(byte)19,(byte)7,(byte)85,
		(byte)110,(byte)107,(byte)110,(byte)111,(byte)119,(byte)110,(byte)49,(byte)14,(byte)48,(byte)12,
		(byte)6,(byte)3,(byte)85,(byte)4,(byte)3,(byte)19,(byte)5,(byte)109,(byte)105,(byte)117,
		(byte)109,(byte)50,(byte)48,(byte)30,(byte)23,(byte)13,(byte)49,(byte)54,(byte)48,(byte)55,
		(byte)49,(byte)51,(byte)48,(byte)54,(byte)49,(byte)57,(byte)52,(byte)56,(byte)90,(byte)23,
		(byte)13,(byte)49,(byte)54,(byte)49,(byte)48,(byte)49,(byte)49,(byte)48,(byte)54,(byte)49,
		(byte)57,(byte)52,(byte)56,(byte)90,(byte)48,(byte)101,(byte)49,(byte)11,(byte)48,(byte)9,
		(byte)6,(byte)3,(byte)85,(byte)4,(byte)6,(byte)19,(byte)2,(byte)107,(byte)114,(byte)49,
		(byte)16,(byte)48,(byte)14,(byte)6,(byte)3,(byte)85,(byte)4,(byte)8,(byte)19,(byte)7,
		(byte)85,(byte)110,(byte)107,(byte)110,(byte)111,(byte)119,(byte)110,(byte)49,(byte)16,(byte)48,
		(byte)14,(byte)6,(byte)3,(byte)85,(byte)4,(byte)7,(byte)19,(byte)7,(byte)85,(byte)110,
		(byte)107,(byte)110,(byte)111,(byte)119,(byte)110,(byte)49,(byte)16,(byte)48,(byte)14,(byte)6,
		(byte)3,(byte)85,(byte)4,(byte)10,(byte)19,(byte)7,(byte)85,(byte)110,(byte)107,(byte)110,
		(byte)111,(byte)119,(byte)110,(byte)49,(byte)16,(byte)48,(byte)14,(byte)6,(byte)3,(byte)85,
		(byte)4,(byte)11,(byte)19,(byte)7,(byte)85,(byte)110,(byte)107,(byte)110,(byte)111,(byte)119,
		(byte)110,(byte)49,(byte)14,(byte)48,(byte)12,(byte)6,(byte)3,(byte)85,(byte)4,(byte)3,
		(byte)19,(byte)5,(byte)109,(byte)105,(byte)117,(byte)109,(byte)50,(byte)48,(byte)130,(byte)1,
		(byte)184,(byte)48,(byte)130,(byte)1,(byte)44,(byte)6,(byte)7,(byte)42,(byte)134,(byte)72,
		(byte)206,(byte)56,(byte)4,(byte)1,(byte)48,(byte)130,(byte)1,(byte)31,(byte)2,(byte)129,
		(byte)129,(byte)0,(byte)253,(byte)127,(byte)83,(byte)129,(byte)29,(byte)117,(byte)18,(byte)41,
		(byte)82,(byte)223,(byte)74,(byte)156,(byte)46,(byte)236,(byte)228,(byte)231,(byte)246,(byte)17,
		(byte)183,(byte)82,(byte)60,(byte)239,(byte)68,(byte)0,(byte)195,(byte)30,(byte)63,(byte)128,
		(byte)182,(byte)81,(byte)38,(byte)105,(byte)69,(byte)93,(byte)64,(byte)34,(byte)81,(byte)251,
		(byte)89,(byte)61,(byte)141,(byte)88,(byte)250,(byte)191,(byte)197,(byte)245,(byte)186,(byte)48,
		(byte)246,(byte)203,(byte)155,(byte)85,(byte)108,(byte)215,(byte)129,(byte)59,(byte)128,(byte)29,
		(byte)52,(byte)111,(byte)242,(byte)102,(byte)96,(byte)183,(byte)107,(byte)153,(byte)80,(byte)165,
		(byte)164,(byte)159,(byte)159,(byte)232,(byte)4,(byte)123,(byte)16,(byte)34,(byte)194,(byte)79,
		(byte)187,(byte)169,(byte)215,(byte)254,(byte)183,(byte)198,(byte)27,(byte)248,(byte)59,(byte)87,
		(byte)231,(byte)198,(byte)168,(byte)166,(byte)21,(byte)15,(byte)4,(byte)251,(byte)131,(byte)246,
		(byte)211,(byte)197,(byte)30,(byte)195,(byte)2,(byte)53,(byte)84,(byte)19,(byte)90,(byte)22,
		(byte)145,(byte)50,(byte)246,(byte)117,(byte)243,(byte)174,(byte)43,(byte)97,(byte)215,(byte)42,
		(byte)239,(byte)242,(byte)34,(byte)3,(byte)25,(byte)157,(byte)209,(byte)72,(byte)1,(byte)199,
		(byte)2,(byte)21,(byte)0,(byte)151,(byte)96,(byte)80,(byte)143,(byte)21,(byte)35,(byte)11,
		(byte)204,(byte)178,(byte)146,(byte)185,(byte)130,(byte)162,(byte)235,(byte)132,(byte)11,(byte)240,
		(byte)88,(byte)28,(byte)245,(byte)2,(byte)129,(byte)129,(byte)0,(byte)247,(byte)225,(byte)160,
		(byte)133,(byte)214,(byte)155,(byte)61,(byte)222,(byte)203,(byte)188,(byte)171,(byte)92,(byte)54,
		(byte)184,(byte)87,(byte)185,(byte)121,(byte)148,(byte)175,(byte)187,(byte)250,(byte)58,(byte)234,
		(byte)130,(byte)249,(byte)87,(byte)76,(byte)11,(byte)61,(byte)7,(byte)130,(byte)103,(byte)81,
		(byte)89,(byte)87,(byte)142,(byte)186,(byte)212,(byte)89,(byte)79,(byte)230,(byte)113,(byte)7,
		(byte)16,(byte)129,(byte)128,(byte)180,(byte)73,(byte)22,(byte)113,(byte)35,(byte)232,(byte)76,
		(byte)40,(byte)22,(byte)19,(byte)183,(byte)207,(byte)9,(byte)50,(byte)140,(byte)200,(byte)166,
		(byte)225,(byte)60,(byte)22,(byte)122,(byte)139,(byte)84,(byte)124,(byte)141,(byte)40,(byte)224,
		(byte)163,(byte)174,(byte)30,(byte)43,(byte)179,(byte)166,(byte)117,(byte)145,(byte)110,(byte)163,
		(byte)127,(byte)11,(byte)250,(byte)33,(byte)53,(byte)98,(byte)241,(byte)251,(byte)98,(byte)122,
		(byte)1,(byte)36,(byte)59,(byte)204,(byte)164,(byte)241,(byte)190,(byte)168,(byte)81,(byte)144,
		(byte)137,(byte)168,(byte)131,(byte)223,(byte)225,(byte)90,(byte)229,(byte)159,(byte)6,(byte)146,
		(byte)139,(byte)102,(byte)94,(byte)128,(byte)123,(byte)85,(byte)37,(byte)100,(byte)1,(byte)76,
		(byte)59,(byte)254,(byte)207,(byte)73,(byte)42,(byte)3,(byte)129,(byte)133,(byte)0,(byte)2,
		(byte)129,(byte)129,(byte)0,(byte)149,(byte)94,(byte)229,(byte)96,(byte)212,(byte)36,(byte)8,
		(byte)23,(byte)100,(byte)23,(byte)17,(byte)164,(byte)165,(byte)71,(byte)9,(byte)191,(byte)0,
		(byte)195,(byte)202,(byte)86,(byte)254,(byte)125,(byte)238,(byte)175,(byte)71,(byte)48,(byte)235,
		(byte)41,(byte)60,(byte)230,(byte)78,(byte)159,(byte)122,(byte)39,(byte)191,(byte)110,(byte)175,
		(byte)146,(byte)66,(byte)172,(byte)108,(byte)19,(byte)178,(byte)69,(byte)69,(byte)34,(byte)67,
		(byte)133,(byte)185,(byte)193,(byte)36,(byte)67,(byte)243,(byte)239,(byte)109,(byte)61,(byte)124,
		(byte)153,(byte)185,(byte)11,(byte)51,(byte)40,(byte)185,(byte)84,(byte)84,(byte)37,(byte)127,
		(byte)212,(byte)1,(byte)59,(byte)109,(byte)86,(byte)8,(byte)149,(byte)135,(byte)200,(byte)223,
		(byte)174,(byte)128,(byte)125,(byte)40,(byte)26,(byte)31,(byte)252,(byte)103,(byte)142,(byte)77,
		(byte)95,(byte)65,(byte)85,(byte)232,(byte)126,(byte)194,(byte)84,(byte)129,(byte)107,(byte)169,
		(byte)165,(byte)182,(byte)228,(byte)29,(byte)125,(byte)242,(byte)83,(byte)176,(byte)105,(byte)55,
		(byte)219,(byte)205,(byte)243,(byte)157,(byte)11,(byte)178,(byte)117,(byte)120,(byte)143,(byte)94,
		(byte)185,(byte)75,(byte)225,(byte)207,(byte)141,(byte)201,(byte)66,(byte)178,(byte)182,(byte)251,
		(byte)232,(byte)163,(byte)33,(byte)48,(byte)31,(byte)48,(byte)29,(byte)6,(byte)3,(byte)85,
		(byte)29,(byte)14,(byte)4,(byte)22,(byte)4,(byte)20,(byte)229,(byte)129,(byte)84,(byte)10,
		(byte)177,(byte)248,(byte)208,(byte)246,(byte)59,(byte)3,(byte)116,(byte)2,(byte)197,(byte)254,
		(byte)52,(byte)141,(byte)94,(byte)250,(byte)36,(byte)218,(byte)48,(byte)11,(byte)6,(byte)7,
		(byte)42,(byte)134,(byte)72,(byte)206,(byte)56,(byte)4,(byte)3,(byte)5,(byte)0,(byte)3,
		(byte)47,(byte)0,(byte)48,(byte)44,(byte)2,(byte)20,(byte)17,(byte)6,(byte)107,(byte)169,
		(byte)148,(byte)37,(byte)133,(byte)153,(byte)57,(byte)144,(byte)235,(byte)95,(byte)58,(byte)144,
		(byte)171,(byte)22,(byte)111,(byte)196,(byte)41,(byte)69,(byte)2,(byte)20,(byte)89,(byte)107,
		(byte)82,(byte)81,(byte)120,(byte)191,(byte)42,(byte)122,(byte)147,(byte)159,(byte)115,(byte)122,
		(byte)72,(byte)191,(byte)79,(byte)71,(byte)96,(byte)97,(byte)243,(byte)15,(byte)213,(byte)123,
		(byte)135,(byte)16,(byte)231,(byte)249,(byte)112,(byte)2,(byte)43,(byte)126,(byte)129,(byte)73,
		(byte)101,(byte)181,(byte)198,(byte)16,(byte)181,(byte)219,(byte)115,(byte)45 };

	
	public static KeyStore getKeyStore()
	{
		KeyStore ks = null;
		try{
		ks = KeyStore.getInstance("JKS");
        ks.load(asInputStream(), getKeyStorePassword());
		}catch(Exception ex){
			throw new RuntimeException("Failed to load SSL key store.", ex);
		}
        return ks;
	}
	
	public static InputStream asInputStream() {		
		return new ByteArrayInputStream(CERT_BYTES);
	}

	public static char[] getCertificatePassword() {
		return "uracle1234".toCharArray();
	}

	public static char[] getKeyStorePassword() {
		return "uracle1234".toCharArray();
	}
	
	public static String getCertificatePasswordString() {
		return "inc0rrect";
	}

	public static String getKeyStorePasswordString() {
		return "mu$tch8ng3";
	}

	private SecureSocketKeyStore() {

	}

}
