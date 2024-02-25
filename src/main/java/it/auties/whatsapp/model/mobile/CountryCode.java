package it.auties.whatsapp.model.mobile;

import java.util.Arrays;
import java.util.Optional;

public enum CountryCode {
    AFGHANISTAN("93", 412, "en", "AF"),
    ALBANIA("355", 276, "sq", "AL"),
    ALGERIA("213", 603, "ar", "DZ"),
    AMERICAN_SAMOA("1-684", 544, "en", "AS"),
    ANDORRA("376", 213, "ca", "AD"),
    ANGOLA("244", 631, "pt", "AO"),
    ANGUILLA("1-264", 365, "en", "AI"),
    ANTIGUA_AND_BARBUDA("1-268", 344, "en", "AG"),
    ARGENTINA("54", 722, "es", "AR"),
    ARMENIA("374", 283, "hy", "AM"),
    ARUBA("297", 363, "nl", "AW"),
    AUSTRALIA("61", 505, "en", "AU"),
    AUSTRIA("43", 232, "de", "AT"),
    AZERBAIJAN("994", 400, "az", "AZ"),
    BAHAMAS("1-242", 364, "en", "BS"),
    BAHRAIN("973", 426, "ar", "BH"),
    BANGLADESH("880", 470, "bn", "BD"),
    BARBADOS("1-246", 342, "en", "BB"),
    BELARUS("375", 257, "be", "BY"),
    BELGIUM("32", 206, "nl", "BE"),
    BELIZE("501", 702, "en", "BZ"),
    BENIN("229", 616, "fr", "BJ"),
    BERMUDA("1-441", 350, "en", "BM"),
    BHUTAN("975", 402, "dz", "BT"),
    BOLIVIA("591", 736, "es", "BO"),
    BOSNIA_AND_HERZEGOVINA("387", 218, "bs", "BA"),
    BOTSWANA("267", 652, "en", "BW"),
    BRAZIL("55", 724, "pt", "BR"),
    BRITISH_VIRGIN_ISLANDS("1-284", 348, "en", "VG"),
    BRUNEI("673", 528, "ms", "BN"),
    BULGARIA("359", 284, "bg", "BG"),
    BURKINA_FASO("226", 613, "fr", "BF"),
    BURUNDI("257", 642, "rn", "BI"),
    CAMBODIA("855", 456, "km", "KH"),
    CAMEROON("237", 624, "fr", "CM"),
    CAPE_VERDE("238", 625, "pt", "CV"),
    CAYMAN_ISLANDS("1-345", 346, "en", "KY"),
    CENTRAL_AFRICAN_REPUBLIC("236", 623, "fr", "CF"),
    CHAD("235", 622, "fr", "TD"),
    CHILE("56", 730, "es", "CL"),
    CHINA("86", 454, "zh", "CN"),
    COLOMBIA("57", 732, "es", "CO"),
    COMOROS("269", 654, "ar", "KM"),
    COOK_ISLANDS("682", 548, "en", "CK"),
    COSTA_RICA("506", 712, "es", "CR"),
    CROATIA("385", 219, "hr", "HR"),
    CUBA("53", 368, "es", "CU"),
    CYPRUS("357", 280, "el", "CY"),
    CZECH_REPUBLIC("420", 230, "cs", "CZ"),
    DEMOCRATIC_REPUBLIC_OF_THE_CONGO("243", 630, "fr", "CD"),
    DENMARK("45", 238, "da", "DK"),
    DJIBOUTI("253", 638, "fr", "DJ"),
    DOMINICA("1-767", 366, "en", "DM"),
    ECUADOR("593", 740, "es", "EC"),
    EGYPT("20", 602, "ar", "EG"),
    EL_SALVADOR("503", 706, "es", "SV"),
    EQUATORIAL_GUINEA("240", 627, "es", "GQ"),
    ERITREA("291", 657, "ti", "ER"),
    ESTONIA("372", 248, "et", "EE"),
    ETHIOPIA("251", 636, "am", "ET"),
    FALKLAND_ISLANDS("500", 750, "en", "FK"),
    FAROE_ISLANDS("298", 288, "fo", "FO"),
    FIJI("679", 542, "en", "FJ"),
    FINLAND("358", 244, "fi", "FI"),
    FRANCE("33", 208, "fr", "FR"),
    FRENCH_POLYNESIA("689", 547, "fr", "PF"),
    GABON("241", 628, "fr", "GA"),
    GAMBIA("220", 607, "en", "GM"),
    GEORGIA("995", 282, "ka", "GE"),
    GERMANY("49", 262, "de", "DE"),
    GHANA("233", 620, "en", "GH"),
    GIBRALTAR("350", 266, "en", "GI"),
    GREECE("30", 202, "el", "GR"),
    GREENLAND("299", 290, "kl", "GL"),
    GRENADA("1-473", 352, "en", "GD"),
    GUAM("1-671", 535, "en", "GU"),
    GUATEMALA("502", 704, "es", "GT"),
    GUINEA("224", 537, "fr", "GN"),
    GUYANA("592", 738, "en", "GY"),
    HAITI("509", 372, "fr", "HT"),
    HONDURAS("504", 708, "es", "HN"),
    HONG_KONG("852", 454, "zh", "HK"),
    HUNGARY("36", 216, "hu", "HU"),
    ICELAND("354", 274, "is", "IS"),
    INDIA("91", 404, "hi", "IN"),
    INDONESIA("62", 510, "id", "ID"),
    IRAN("98", 432, "fa", "IR"),
    IRAQ("964", 418, "ar", "IQ"),
    IRELAND("353", 234, "en", "IE"),
    ISRAEL("972", 425, "he", "IL"),
    ITALY("39", 222, "it", "IT"),
    IVORY_COAST("225", 612, "fr", "CI"),
    JAMAICA("1-876", 338, "en", "JM"),
    JAPAN("81", 440, "ja", "JP"),
    JORDAN("962", 416, "ar", "JO"),
    KAZAKHSTAN("7", 401, "kk", "KZ"),
    KENYA("254", 639, "sw", "KE"),
    KIRIBATI("686", 545, "en", "KI"),
    KOSOVO("383", 221, "sq", "XK"),
    KUWAIT("965", 419, "ar", "KW"),
    LATVIA("371", 247, "lv", "LV"),
    LEBANON("961", 415, "ar", "LB"),
    LESOTHO("266", 651, "en", "LS"),
    LIBERIA("231", 618, "en", "LR"),
    LIBYA("218", 606, "ar", "LY"),
    LIECHTENSTEIN("423", 295, "de", "LI"),
    LITHUANIA("370", 246, "lt", "LT"),
    LUXEMBOURG("352", 270, "lb", "LU"),
    MACAO("853", 455, "zh", "MO"),
    MACEDONIA("389", 294, "mk", "MK"),
    MADAGASCAR("261", 646, "mg", "MG"),
    MALAWI("265", 650, "en", "MW"),
    MALAYSIA("60", 502, "ms", "MY"),
    MALDIVES("960", 472, "dv", "MV"),
    MALI("223", 610, "fr", "ML"),
    MALTA("356", 278, "mt", "MT"),
    MARSHALL_ISLANDS("692", 551, "en", "MH"),
    MAURITANIA("222", 609, "ar", "MR"),
    MAURITIUS("230", 617, "en", "MU"),
    MEXICO("52", 334, "es", "MX"),
    MICRONESIA("691", 550, "en", "FM"),
    MOLDOVA("373", 259, "ro", "MD"),
    MONACO("377", 212, "fr", "MC"),
    MONGOLIA("976", 428, "mn", "MN"),
    MONTENEGRO("382", 297, "sr", "ME"),
    MONTSERRAT("1-664", 354, "en", "MS"),
    MOROCCO("212", 604, "ar", "MA"),
    MOZAMBIQUE("258", 643, "pt", "MZ"),
    MYANMAR("95", 414, "my", "MM"),
    NAMIBIA("264", 649, "en", "NA"),
    NAURU("674", 536, "en", "NR"),
    NEPAL("977", 429, "ne", "NP"),
    NETHERLANDS("31", 204, "nl", "NL"),
    NEW_CALEDONIA("687", 546, "fr", "NC"),
    NEW_ZEALAND("64", 530, "en", "NZ"),
    NICARAGUA("505", 710, "es", "NI"),
    NIGER("227", 614, "fr", "NE"),
    NIGERIA("234", 621, "en", "NG"),
    NIUE("683", 555, "en", "NU"),
    NORTHERN_MARIANA_ISLANDS("1-670", 534, "en", "MP"),
    NORWAY("47", 242, "no", "NO"),
    OMAN("968", 226, "ar", "OM"),
    PAKISTAN("92", 410, "ur", "PK"),
    PALAU("680", 552, "en", "PW"),
    PALESTINE("970", 423, "ar", "PS"),
    PANAMA("507", 714, "es", "PA"),
    PAPUA_NEW_GUINEA("675", 537, "en", "PG"),
    PARAGUAY("595", 744, "es", "PY"),
    PERU("51", 716, "es", "PE"),
    PHILIPPINES("63", 515, "en", "PH"),
    POLAND("48", 260, "pl", "PL"),
    PORTUGAL("351", 268, "pt", "PT"),
    QATAR("974", 427, "ar", "QA"),
    REPUBLIC_OF_THE_CONGO("242", 630, "fr", "CG"),
    ROMANIA("40", 226, "ro", "RO"),
    RUSSIA("7", 250, "ru", "RU"),
    RWANDA("250", 635, "rw", "RW"),
    SAINT_HELENA("290", 658, "en", "SH"),
    SAINT_KITTS_AND_NEVIS("1-869", 356, "en", "KN"),
    SAINT_LUCIA("1-758", 358, "en", "LC"),
    SAINT_PIERRE_AND_MIQUELON("508", 308, "fr", "PM"),
    SAINT_VINCENT_AND_THE_GRENADINES("1-784", 360, "en", "VC"),
    SAMOA("685", 544, "en", "WS"),
    SAN_MARINO("378", 292, "it", "SM"),
    SAO_TOME_AND_PRINCIPE("239", 626, "pt", "ST"),
    SAUDI_ARABIA("966", 420, "ar", "SA"),
    SENEGAL("221", 608, "fr", "SN"),
    SERBIA("381", 220, "sr", "RS"),
    SEYCHELLES("248", 633, "fr", "SC"),
    SIERRA_LEONE("232", 619, "en", "SL"),
    SINGAPORE("65", 525, "en", "SG"),
    SLOVENIA("386", 293, "sl", "SI"),
    SOLOMON_ISLANDS("677", 540, "en", "SB"),
    SOUTH_AFRICA("27", 655, "en", "ZA"),
    SOUTH_SUDAN("211", 659, "en", "SS"),
    SPAIN("34", 214, "es", "ES"),
    SRI_LANKA("94", 413, "si", "LK"),
    SUDAN("249", 634, "ar", "SD"),
    SURINAME("597", 746, "nl", "SR"),
    SWAZILAND("268", 653, "ss", "SZ"),
    SWEDEN("46", 240, "sv", "SE"),
    SWITZERLAND("41", 228, "de", "CH"),
    SYRIA("963", 417, "ar", "SY"),
    TAIWAN("886", 466, "zh", "TW"),
    TAJIKISTAN("992", 436, "tg", "TJ"),
    TANZANIA("255", 640, "sw", "TZ"),
    THAILAND("66", 520, "th", "TH"),
    TOGO("228", 615, "fr", "TG"),
    TOKELAU("690", 554, "en", "TK"),
    TONGA("676", 539, "to", "TO"),
    TRINIDAD_AND_TOBAGO("1-868", 374, "en", "TT"),
    TUNISIA("216", 605, "ar", "TN"),
    TURKEY("90", 286, "tr", "TR"),
    TURKMENISTAN("993", 438, "tk", "TM"),
    TURKS_AND_CAICOS_ISLANDS("1-649", 376, "en", "TC"),
    TUVALU("688", 553, "en", "TV"),
    US_VIRGIN_ISLANDS("1-340", 332, "en", "VI"),
    UGANDA("256", 641, "en", "UG"),
    UKRAINE("380", 255, "uk", "UA"),
    UNITED_ARAB_EMIRATES("971", 424, "ar", "AE"),
    UNITED_KINGDOM("44", 234, "en", "GB"),
    UNITED_STATES("1", 310, "en", "US"),
    URUGUAY("598", 748, "es", "UY"),
    UZBEKISTAN("998", 434, "uz", "UZ"),
    VANUATU("678", 541, "bi", "VU"),
    VATICAN("379", 225, "it", "VA"),
    VENEZUELA("58", 734, "es", "VE"),
    WALLIS_AND_FUTUNA("681", 543, "fr", "WF"),
    YEMEN("967", 421, "ar", "YE"),
    ZAMBIA("260", 645, "en", "ZM"),
    ZIMBABWE("263", 648, "en", "ZW"),
    EAST_TIMOR("670", 514, "pt", "TL"),
    GUINEA_BISSAU("245", 632, "pt", "GW"),
    LAOS("856", 457, "lo", "LA"),
    NETHERLANDS_ANTILLES("599", 362, "nl", "AN"),
    NORTH_KOREA("850", 467, "ko", "KP"),
    REUNION("262", 647, "fr", "RE"),
    SOUTH_KOREA("82", 450, "ko", "KR"),
    VIETNAM("84", 452, "vi", "VN");

    private final String prefix;
    private final int mcc;
    private final String lg;
    private final String lc;

    CountryCode(String prefix, int mcc, String lg, String lc) {
        this.prefix = prefix;
        this.mcc = mcc;
        this.lg = lg;
        this.lc = lc;
    }

    public static Optional<CountryCode> ofPrefix(String prefix) {
        return prefix == null ? Optional.empty() : Arrays.stream(values())
                .filter(entry -> entry.prefix().equalsIgnoreCase(prefix))
                .findFirst();
    }

    public String prefix() {
        return prefix;
    }

    public String mcc() {
        return String.valueOf(mcc);
    }

    public String lc() {
        return lc;
    }

    public String lg() {
        return lg;
    }
}
