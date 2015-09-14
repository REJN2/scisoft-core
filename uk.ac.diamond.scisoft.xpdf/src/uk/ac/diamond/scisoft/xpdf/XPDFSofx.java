/*-
 * Copyright 2015 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.xpdf;

/**
 * Calculates inelastic form factors following Balyuzi (1975).
 * <p>
 * An effectively static class for holding the incoherent scattering factors.
 *  Derived from Balyuzi, H. H. M., "Analytic Approximations to Incoherently
 *  Scattered X-Ray Intensities", Acta Crystallographica, A31, pp. 600-603 (1975)
 *  via ESRF's DABAX database and the Fortran library of S. Brennan and 
 *  P.L. Cowan (1992) Rev. Sci. Instrum. 63,1, 850.
 * <p>
 *  Neutral atoms only, hydrogen ([0]) up to and including americium ([94]).
 */
public final class XPDFSofx {
	
	static int maxZ = 95;
	
	// Indices go from 0, atomic number from 1
	/**
	 * Return the array of "a" constants.
	 * @param z
	 * 			atomic number
	 * @return the multiplicative exponential scaling constants.
	 */
	public static double[] getA(int z) {
		return a[Integer.min(z, maxZ)-1];
	}
	
	/**
	 * Return the array of "b" constants.
	 * @param z
	 * 			atomic number
	 * @return the exponential width constants.
	 */
	public static double[] getB(int z) {
		return b[Integer.min(z, maxZ)-1];
	}
	
	/**
	 * Return the "c" constant.
	 * <p>
	 * Balyuzi (1975) does not include a constant term, but we can recover the
	 * incoherent scattering function directly by setting c to z.
	 * @param z
	 * 			atomic number
	 * @return the additive constant.
	 */
	public static double getC(int z) {
		return (double) z;
	}

	/**
	 * Negative of the a coefficients given by Balyuzi. Combined with the c=Z
	 * constant term, this gives the incoherent scattering function directly
	 */
	static double[][] a = {
		{-0.262300014, -0.509400010, -0.203400001, -2.490000054E-02, -0.000000000E+00},
		{-0.524600029, -1.01880002, -0.406800002, -4.980000108E-02, -0.000000000E+00},
		{-5.180000141E-02, -0.957799971, -0.734799981, -1.08169997, -0.173999995},
		{-0.463400006, -1.55920005, -0.768499970, -1.06229997, -0.147000000},
		{-0.904600024, -1.98220003, -0.227899998, -1.48730004, -0.397899985},
		{-0.756799996, -2.55110002, -0.705100000, -1.46050000, -0.526300013},
		{-0.907000005, -2.89720011, -1.16589999, -1.55260003, -0.476900011},
		{-0.884700000, -3.21889997, -1.79900002, -1.55379999, -0.543399990},
		{-0.975600004, -3.51009989, -2.35610008, -1.58959997, -0.588299990},
		{-1.15439999, -3.80329990, -2.80850005, -1.66470003, -0.568700016},
		{-1.02429998, -2.07040000, -5.31969976, -1.52139997, -1.06369996},
		{-2.00489998, -1.94490004, -5.42910004, -1.63150001, -0.988799989},
		{-2.67899990, -1.23360002, -5.93580008, -1.66219997, -1.48350000},
		{-2.10060000, -2.20250010, -5.30630016, -2.51620007, -1.87160003},
		{-1.89359999, -3.37820005, -5.37519979, -2.49419999, -1.85640001},
		{-2.07170010, -4.20230007, -5.68830013, -2.26230001, -1.77349997},
		{-2.12210011, -5.09049988, -5.68849993, -2.28090000, -1.81640005},
		{-2.17779994, -5.97910023, -5.61040020, -2.34419990, -1.88730001},
		{-1.08630002, -2.85619998, -5.66160011, -6.78090000, -2.61450005},
		{-2.05539989, -2.72510004, -5.91039991, -6.71710014, -2.59159994},
		{-2.16079998, -2.95449996, -6.51249981, -6.77449989, -2.59689999},
		{-2.19829988, -3.22819996, -7.06489992, -6.88320017, -2.62430000},
		{-2.23990011, -3.43479991, -7.64120007, -7.02699995, -2.65580010},
		{-1.58930004, -3.66919994, -8.72490025, -7.29129982, -2.72259998},
		{-2.31559992, -3.74200010, -8.82409954, -7.38110018, -2.73550010},
		{-2.31410003, -3.97289991, -9.26239967, -7.50829983, -2.94039989},
		{-2.36360002, -4.41879988, -9.73499966, -7.68720007, -2.79270005},
		{-2.41739988, -4.70760012, -10.2289000, -7.83570004, -2.80710006},
		{-1.75320005, -4.81669998, -11.2835999, -8.24600029, -2.89429998},
		{-2.44740009, -5.10799980, -11.4406996, -8.17910004, -2.82110000},
		{-3.03600001, -5.17600012, -11.6691999, -8.29399967, -2.81480002},
		{-3.47950006, -5.23059988, -11.9323997, -8.50629997, -2.83879995},
		{-3.33850002, -5.62290001, -12.2448997, -8.86680031, -2.91380000},
		{-3.53329992, -5.96439981, -12.4403000, -9.12119961, -2.92890000},
		{-3.48979998, -6.56409979, -12.5318003, -9.38770008, -3.01699996},
		{-3.05979991, -7.66709995, -12.5852003, -9.61680031, -3.06469989},
		{-2.37700009, -8.66049957, -12.5728998, -10.1506996, -3.20830011},
		{-3.07399988, -8.83440018, -12.5144997, -10.3128996, -3.24329996},
		{-3.50860000, -9.25739956, -12.4296999, -10.4961996, -3.28399992},
		{-3.84360003, -9.69799995, -12.3352003, -10.7459002, -3.34929991},
		{-3.75189996, -10.5738001, -12.2250996, -10.9744997, -3.44250011},
		{-3.95670009, -11.1227999, -12.1674004, -11.2327995, -3.48569989},
		{-4.33169985, -10.7065001, -12.1316004, -12.0686998, -3.72959995},
		{-4.73080015, -12.0615997, -11.9366999, -11.6021004, -3.62249994},
		{-4.87449980, -12.2804003, -12.0132999, -12.0244999, -3.75920010},
		{-5.48470020, -13.7150002, -11.6608000, -11.4708996, -3.65450001},
		{-5.34899998, -13.4126997, -11.9401999, -12.3200998, -3.92050004},
		{-4.52239990, -9.62819958, -14.9509001, -14.2066002, -4.65630007},
		{-4.70310020, -8.23610020, -16.4206009, -14.6608000, -4.94929981},
		{-4.96770000, -7.48729992, -17.4267998, -14.9229002, -5.16779995},
		{-4.60010004, -7.43989992, -18.3474998, -15.1777000, -5.40959978},
		{-4.56710005, -7.71780014, -18.8337994, -15.2844000, -5.57690001},
		{-4.31470013, -8.37279987, -19.1961002, -15.3645000, -5.73649979},
		{-3.44670010, -19.4724007, -9.74730015, -15.4209995, -5.90339994},
		{-2.57509995, -19.7180996, -11.0137997, -15.5312004, -6.13740015},
		{-3.24769998, -19.7824001, -11.2142000, -15.4790001, -6.26020002},
		{-3.66829991, -11.7149000, -19.8076992, -15.4014997, -6.38600016},
		{-3.72180009, -12.0075998, -20.1924992, -15.4540005, -6.59959984},
		{-3.62870002, -11.9193001, -20.8822994, -15.6424999, -6.89709997},
		{-3.72289991, -12.0836000, -21.2663994, -15.7405005, -7.15210009},
		{-3.80089998, -12.2545996, -21.6280994, -15.8659000, -7.41260004},
		{-3.85330009, -12.3520002, -22.0135994, -16.0323009, -7.70819998},
		{-3.92339993, -12.4961004, -22.3132992, -16.2152004, -8.00850010},
		{-4.11000013, -13.0008001, -22.2961998, -16.2936993, -8.25940037},
		{-4.15889978, -13.2196999, -22.4626999, -16.5328999, -8.58419991},
		{-4.04150009, -13.1037998, -22.8132000, -17.0065994, -8.98490047},
		{-4.06199980, -13.2131996, -22.9351006, -17.3999004, -9.33880043},
		{-4.08150005, -13.3282003, -22.9864998, -17.8547993, -9.69670010},
		{-4.74160004, -13.2875004, -23.1429005, -17.9214993, -9.89350033},
		{-4.10139990, -13.4586000, -22.9675007, -18.9997997, -10.4188004},
		{-4.32609987, -13.8638000, -22.6991997, -19.3649006, -10.6985998},
		{-4.57590008, -14.3291998, -22.3740997, -19.7026997, -10.9720001},
		{-4.74160004, -14.9284000, -22.0214005, -20.0289993, -11.2347002},
		{-4.84229994, -15.6421003, -21.6700993, -20.3206997, -11.4816999},
		{-5.00150013, -16.3873997, -21.3262997, -20.5247002, -11.7163000},
		{-5.36299992, -17.5065994, -23.3197002, -20.9424992, -8.81579971},
		{-5.09530020, -15.4321003, -15.5923996, -26.5617008, -14.2784004},
		{-5.64510012, -18.8015003, -20.6881008, -20.5265999, -12.2868004},
		{-5.81699991, -19.5634995, -20.3715992, -20.6837006, -12.5114002},
		{-5.93889999, -19.4946003, -19.0547009, -22.4442005, -13.0068998},
		{-6.58029985, -19.6009998, -18.3955002, -23.0433006, -13.3134003},
		{-7.37540007, -19.7973995, -18.0002003, -23.2605000, -13.4942999},
		{-8.23239994, -20.0182991, -17.6602993, -23.3246002, -13.6777000},
		{-9.06620026, -20.1394997, -23.6273003, -17.1872005, -13.8913002},
		{-9.92230034, -20.2544003, -23.9015007, -16.7271004, -14.1028004},
		{-10.7789001, -20.3925991, -16.3719997, -24.0636997, -14.2879000},
		{-2.53320003, -11.5319004, -24.7458992, -31.9246998, -16.2474995},
		{-3.22490001, -11.6694002, -24.8687000, -31.8957005, -16.3306007},
		{-3.62069988, -12.1268997, -24.9363003, -31.8798008, -16.4227009},
		{-4.01809978, -12.6119003, -24.9620991, -31.8645992, -16.5249004},
		{-3.79259992, -12.6625996, -25.8794994, -31.9403000, -16.7045002},
		{-3.90510011, -12.8415003, -26.4298000, -31.9514008, -16.8474998},
		{-4.0333, -12.9014, -26.7571, -31.4732, -17.8065},
		{-4.2695, -12.5662, -28.0362, -31.9382, -17.1487},
		{-4.3981, -12.6808, -28.6781, -31.9085, -17.2883}
	};
	
	static double[][] b = {
		{32.3717003, 14.7083998, 6.68839979, 2.48429990, 0.000000000E+00},
		{32.3717003, 14.7083998, 6.68839979, 2.48429990, 0.000000000E+00},
		{533.921997, 245.845001, 10.1830997, 4.43639994, 1.50310004},
		{185.856003, 104.600998, 4.85890007, 2.19320011, 0.764100015},
		{104.960999, 46.0191002, 8.98729992, 1.96739995, 0.677800000},
		{82.2385025, 31.7282009, 11.9470997, 1.46370006, 0.514999986},
		{64.1555023, 20.8507004, 7.75759983, 1.03349996, 0.351599991},
		{52.0063019, 16.4487000, 6.59579992, 0.814300001, 0.281500012},
		{41.7193985, 12.7747002, 5.29449987, 0.647000015, 0.225400001},
		{34.2566986, 9.76720047, 4.03749990, 0.525600016, 0.180000007},
		{293.411011, 15.2372999, 4.46969986, 0.624599993, 0.192200005},
		{178.983002, 11.2433004, 3.42720008, 0.490700006, 0.154200003},
		{121.362999, 16.0727997, 3.26320004, 0.661599994, 0.158600003},
		{116.957001, 34.7760010, 3.32150006, 0.993300021, 0.155300006},
		{108.032997, 26.6585999, 2.68650007, 0.797299981, 0.133300006},
		{86.7210999, 21.4573994, 2.12549996, 0.588199973, 0.112800002},
		{73.8395004, 17.9755993, 1.80369997, 0.516200006, 0.100800000},
		{65.6186981, 14.4379997, 1.56110001, 0.471599996, 9.139999747E-02},
		{425.473999, 35.7248993, 9.32610035, 1.02059996, 0.103600003},
		{289.862000, 28.7189999, 7.47879982, 0.864400029, 9.200000018E-02},
		{251.386993, 26.8528004, 6.42379999, 0.761600018, 8.309999853E-02},
		{226.968002, 24.9305992, 5.62130022, 0.686500013, 7.580000162E-02},
		{206.399002, 22.9025993, 4.93660021, 0.625599980, 6.970000267E-02},
		{196.584000, 25.2663002, 4.58479977, 0.587599993, 6.499999762E-02},
		{174.656006, 19.5879002, 3.88960004, 0.532599986, 5.970000103E-02},
		{164.104004, 18.2898006, 3.58610010, 0.515500009, 5.970000103E-02},
		{151.337006, 16.0769997, 3.14450002, 0.455300003, 5.139999837E-02},
		{139.962997, 14.5797005, 2.81419992, 0.420300007, 4.760000110E-02},
		{127.250999, 16.9193993, 2.74580002, 0.407599986, 4.540000111E-02},
		{125.083000, 12.8443003, 2.31220007, 0.360500008, 4.100000113E-02},
		{104.577003, 11.2803001, 2.06629992, 0.331400007, 3.799999878E-02},
		{87.5304031, 10.8491001, 1.90600002, 0.310699999, 3.559999913E-02},
		{82.4878998, 12.0723000, 1.83169997, 0.299600005, 3.409999982E-02},
		{73.4400024, 12.2369003, 1.71510005, 0.282400012, 3.180000186E-02},
		{68.5438004, 12.5939999, 1.62349999, 0.271200001, 3.079999983E-02},
		{70.8068008, 12.6449003, 1.52300000, 0.257699996, 2.940000035E-02},
		{178.867004, 13.9200001, 1.52240002, 0.256700009, 2.889999934E-02},
		{210.802994, 12.3783998, 1.41480005, 0.242400005, 2.749999985E-02},
		{177.423004, 11.0509005, 1.32420003, 0.230000004, 2.630000003E-02},
		{152.274002, 10.0160999, 1.26100004, 0.220599994, 2.539999969E-02},
		{117.447998, 8.99750042, 1.20439994, 0.212699994, 2.480000071E-02},
		{105.180000, 8.07540035, 1.15509999, 0.203799993, 2.370000072E-02},
		{115.939003, 7.91510010, 1.27690005, 0.210500002, 2.400000021E-02},
		{77.6118011, 6.46670008, 1.04310000, 0.187700003, 2.229999937E-02},
		{71.5772018, 6.17010021, 1.05890000, 0.185299993, 2.199999988E-02},
		{53.2508011, 4.93289995, 0.857500017, 0.165500000, 2.060000040E-02},
		{58.9663010, 5.05480003, 0.968400002, 0.171499997, 2.099999972E-02},
		{87.3897018, 7.71190023, 1.58200002, 0.203600004, 0.233999997},
		{89.2096024, 8.99650002, 1.72290003, 0.206200004, 2.380000055E-02},
		{83.2133026, 10.1129999, 1.76049995, 0.204200000, 2.370000072E-02},
		{62.5070000, 12.5902004, 1.79059994, 0.202900007, 2.380000055E-02},
		{79.7245026, 13.8028002, 1.74109995, 0.197600007, 0.234999999},
		{78.6996002, 14.9420996, 1.67949998, 0.192100003, 2.319999970E-02},
		{88.3050003, 1.61310005, 16.1669006, 0.186900005, 2.290000021E-02},
		{224.598007, 1.57219994, 17.7908001, 0.184499994, 2.290000021E-02},
		{266.592987, 1.47580004, 16.2709999, 0.177499995, 2.250000089E-02},
		{224.725998, 14.7472000, 1.38230002, 0.170800000, 2.219999954E-02},
		{212.565994, 14.0417004, 1.32720006, 0.168099999, 2.209999971E-02},
		{208.102997, 13.8486004, 1.30250001, 0.169000000, 2.219999954E-02},
		{194.998001, 13.2282000, 1.25730002, 0.167699993, 2.219999954E-02},
		{184.339996, 12.6793003, 1.21389997, 0.166899994, 2.219999954E-02},
		{176.391998, 12.2877998, 1.18099999, 0.167199999, 2.229999937E-02},
		{167.815994, 11.8294001, 1.14330006, 0.167600006, 2.239999920E-02},
		{160.858994, 11.0492001, 1.09140003, 0.165900007, 2.229999937E-02},
		{155.186005, 10.6407003, 1.06669998, 0.167400002, 2.239999920E-02},
		{150.057999, 20.6511002, 1.07079995, 0.172299996, 2.270000055E-02},
		{145.975998, 10.3971996, 1.05690002, 0.175200000, 2.280000038E-02},
		{142.072006, 10.1525002, 1.04380000, 0.178499997, 2.290000021E-02},
		{100.169998, 9.06760025, 0.983299971, 0.175300002, 2.270000055E-02},
		{135.832993, 9.81250000, 1.02900004, 0.186000004, 2.309999987E-02},
		{133.360001, 9.16639996, 0.995100021, 0.186100006, 2.300000004E-02},
		{124.500999, 8.51949978, 0.959900022, 0.185699999, 2.290000021E-02},
		{117.648003, 7.90700006, 0.925000012, 0.185000002, 2.270000055E-02},
		{112.694000, 7.35510015, 0.889199972, 0.183699995, 2.260000072E-02},
		{106.000999, 6.75920010, 0.849099994, 0.181700006, 2.239999920E-02},
		{93.6155014, 6.05690002, 0.715600014, 0.137300000, 1.269999985E-02},
		{98.2593994, 6.94630003, 1.25500000, 0.259999990, 2.419999987E-02},
		{69.2677002, 5.31269979, 0.710699975, 0.170200005, 2.160000056E-02},
		{65.4078979, 4.94689989, 0.680899978, 0.167999998, 0.214000002},
		{74.0106964, 4.91179991, 0.737500012, 0.177900001, 2.160000056E-02},
		{69.9997025, 4.60319996, 0.731199980, 0.178700000, 2.160000056E-02},
		{62.4634018, 4.25610018, 0.702099979, 0.175699994, 2.129999921E-02},
		{53.0479012, 3.92490005, 0.667500019, 0.172199994, 2.099999972E-02},
		{48.3272018, 3.66240001, 0.170100003, 0.647099972, 2.080000006E-02},
		{43.7514000, 3.42289996, 0.167999998, 0.627099991, 2.060000040E-02},
		{38.6120987, 3.20499992, 0.603699982, 0.165000007, 2.030000091E-02},
		{269.877991, 21.9610004, 2.07159996, 0.224299997, 2.229999937E-02},
		{321.662994, 20.3845997, 1.97300005, 0.216600001, 2.190000005E-02},
		{270.773987, 18.9025002, 1.88110006, 0.209500000, 2.150000073E-02},
		{232.371002, 17.3824997, 1.79499996, 0.202999994, 2.109999955E-02},
		{236.802994, 17.5907993, 1.76069999, 0.198400006, 2.089999989E-02},
		{221.177994, 16.8736992, 1.70079994, 0.193200007, 2.050000057E-02},
		{207.7270, 16.3175, 1.6677, 0.1954, 0.0220},
		{185.9550, 15.5936, 1.5914, 0.1834, 0.0200},
		{174.3590, 14.9676, 1.5304, 0.1786, 0.0197}
	};
	
}
