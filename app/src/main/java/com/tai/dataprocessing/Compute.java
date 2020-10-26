package com.tai.dataprocessing;

import android.util.Log;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

class Compute {
    private List<BigDecimal> someData;
    private BigDecimal ub;
    private BigDecimal ave;
    // 测量次数2-10
    private BigDecimal[] t_n = new BigDecimal[] {
            BigDecimal.valueOf(8.99), BigDecimal.valueOf(2.48), BigDecimal.valueOf(1.59),
            BigDecimal.valueOf(1.24), BigDecimal.valueOf(1.05), BigDecimal.valueOf(0.93),
            BigDecimal.valueOf(0.84), BigDecimal.valueOf(0.77), BigDecimal.valueOf(0.72)
    };

    Compute(List<BigDecimal> someData, BigDecimal ub) {
        this.someData = someData;
        this.ub = ub;
        ave = someData.get(0);
    }

    private BigDecimal getUa(){
        if (someData.size() < 2)
            throw new DataProcessingExcept("实验数据少于两个，没有A类不确定度");
        BigDecimal sd = getStandardDeviation();
        Log.d("TAG", "sd=" + sd);
        return sd.multiply(t_n[someData.size() - 2]);
    }

    private String getU() {
        BigDecimal u;
        if (someData.size() < 2)
            u = ub;
        else {
            u = sqrt(getUa().pow(2).add(ub.pow(2)));
            Log.d("TAG", "ave=" + ave + "  ua=" + getUa() + "  ub=" + ub + "  u=" + u);
        }
        String uStr = u.toString();
        int pointLoc = uStr.indexOf('.');
        int len = uStr.length();
        int tag;
        char baseNum = '0';
        for (tag = 0; tag < len; tag++) {
            char c = uStr.charAt(tag);
            if (c > '0') {
                baseNum = c;
                break;
            }
        }
        if (baseNum != '0')
            if (u.compareTo(BigDecimal.ONE) < 0) {// u < 0 或 0 < u < 1
                Map<String, String> map = getSrcWithOther(uStr, pointLoc);
                if (map != null) {
                    uStr = map.get("src");
                    pointLoc = 1;
                    assert uStr != null;
                    len = uStr.length();
                    tag = Integer.parseInt(Objects.requireNonNull(map.get("tag")));
                }
                if (baseNum < '5') {
                    if (tag == len - 1)// eg: 0.2 返回 0.20
                        return uStr + '0';
                    for (int i = tag + 2; i < len; i++) {
                        if (uStr.charAt(i) > '0') {
                            String tempResult = getSrcData(new BigDecimal(uStr.substring(0, tag + 2)).add(BigDecimal.valueOf(0.1).pow(tag + 1 - pointLoc)).toString());
                            if (tempResult.length() == tag + 1)// eg: 0.029 + 0.001 = 0.03
                                tempResult += '0';
                            return tempResult;
                        }
                    }
                    return uStr.substring(0, tag + 2);
                } else {
                    if (tag == len - 1)// eg: 0.6 返回 0.6
                        return uStr;
                    for (int i = tag + 1; i < len; i++) {
                        if (uStr.charAt(i) > '0') {
                            String tempResult = getSrcData(new BigDecimal(uStr.substring(0, tag + 1)).add(BigDecimal.valueOf(0.1).pow(tag - pointLoc)).toString());
                            if (baseNum == '9')
                                tempResult = tempResult.substring(0, tempResult.length() - 1);
                            return tempResult;
                        }
                    }
                    return uStr.substring(0, tag + 1);
                }
            } else if (u.compareTo(BigDecimal.valueOf(10)) < 0) {// 1 <= u < 10
                if (baseNum != '9') {
                    if (baseNum < '5') {// (0, 5)
                        if (len < 3)
                            return uStr + ".0";
                        for (int i = 3; i < len; i++) {
                            if (uStr.charAt(i) > '0') {
                                return new BigDecimal(uStr.substring(0, 3)).add(BigDecimal.valueOf(0.1)).toString();
                            }
                        }
                        return uStr.substring(0, 3);
                    } else {// [5, 10)
                        if (len < 3)
                            return uStr;
                        for (int i = 2; i < len; i++) {
                            if (uStr.charAt(i) > '0') {
                                return String.valueOf(Integer.parseInt(uStr.substring(0, 1)) + 1);
                            }
                        }
                        return uStr.substring(0, 1);
                    }
                } else {
                    if (pointLoc < 0)
                        return "9";
                    for (int i = pointLoc + 1; i < len; i++) {
                        if (uStr.charAt(i) > '0')
                            return "1 x 10";
                    }
                    return "9";
                }
            } else if (u.compareTo(BigDecimal.valueOf(50)) < 0) {// 10 <= u < 50
                for (int j = 3; j < len; j++) {
                    if (uStr.charAt(j) > '0')
                        return String.valueOf(Integer.parseInt(uStr.substring(0, 2)) + 1);
                }
                return uStr.substring(0, 2);
            } else {// u >= 50
                if (baseNum < '5')
                    return amendData(uStr, true, pointLoc);
                else
                    return amendData(uStr, false, pointLoc);
            }
        else
            return "0.0";
    }

    private String amendData(String src, boolean two, int pointLoc) {
        // 在 u >= 50 时可用
        String part = "00";
        int zeroLen;
        boolean add = false;
        if (pointLoc > 0) {
            for (int i = pointLoc + 1; i < src.length(); i++) {
                if (src.charAt(i) > '0') {
                    add = true;
                    break;
                }
            }
            src = src.substring(0, pointLoc);
        }
        if (two) {
            zeroLen = 1;
            int i;
            for (i = 2; i < src.length(); i++) {
                zeroLen++;
                if (add || src.charAt(i) > '0') {
                    part = String.valueOf(Integer.parseInt(src.substring(0, 2)) + 1);
                    break;
                }
            }
            if (i < src.length())
                for (int j = i + 1; j < src.length(); j++)
                    zeroLen++;
            else
                part = src.substring(0, 2);
            if (zeroLen > 1)
                return part.charAt(0) + "." + part.charAt(1) + " x 10^" + zeroLen;
            return part.charAt(0) + "." + part.charAt(1) + " x 10";
        } else {
            if (src.charAt(0) != '9') {// [500...000, 900...000)
                zeroLen = 0;
                int i;
                for (i = 1; i < src.length(); i++) {
                    zeroLen++;
                    if (add || src.charAt(i) > '0') {
                        part = String.valueOf(Integer.parseInt(src.substring(0, 1)) + 1);
                        break;
                    }
                }
                if (i < src.length())
                    for (int j = i + 1; j < src.length(); j++)
                        zeroLen++;
                else
                    part = src.substring(0, 1);
                if (zeroLen > 1)
                    return part + " x 10^" + zeroLen;
                return part + " x 10";
            } else {// [900...000, 1000...000)
                for (int i = 1; i < src.length(); i++) {
                    if (add || src.charAt(i) > '0')
                        return "1 x 10^" + src.length();
                }
                if (src.length() > 2)
                    return "9 x 10^" + (src.length() - 1);
                return "9 x 10";
            }
        }
    }

    private Map<String, String> getSrcWithOther(String bigDecimal, int pointLoc) {
        int eTag;
        if ((eTag = bigDecimal.indexOf('E')) >= 0 || (eTag = bigDecimal.indexOf('e')) >= 0) {
            int zeroLen = Integer.parseInt(bigDecimal.substring(eTag + 2));
            StringBuffer tempU = new StringBuffer("0.");
            for (int i = 0; i < zeroLen - 1; i++) {
                tempU.append('0');
            }
            tempU.append(bigDecimal.charAt(0));
            if (pointLoc > 0)
                tempU.append(bigDecimal, pointLoc + 1, eTag);
            bigDecimal = String.valueOf(tempU);
            Map<String, String> map = new HashMap<>();
            map.put("src", bigDecimal);
            map.put("tag", String.valueOf(zeroLen + 1));
            return map;
        }
        return null;
    }

    private String getSrcData(String bigDecimal) {
        int eTag, pointLoc = bigDecimal.indexOf('.');
        if ((eTag = bigDecimal.indexOf('E')) >= 0 || (eTag = bigDecimal.indexOf('e')) >= 0) {
            int zeroLen = Integer.parseInt(bigDecimal.substring(eTag + 2));
            StringBuffer tempNum = new StringBuffer("0.");
            for (int i = 0; i < zeroLen - 1; i++) {
                tempNum.append('0');
            }
            tempNum.append(bigDecimal.charAt(0));
            if (pointLoc > 0)
                tempNum.append(bigDecimal, pointLoc + 1, eTag);
            return String.valueOf(tempNum);
        }
        return bigDecimal;
    }

    /**
     * 计算数据的标准偏差.
     *
     * @return 标准偏差
     */
    private BigDecimal getStandardDeviation() {
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal data : someData) {
            sum = sum.add(data);
        }
        ave = sum.divide(BigDecimal.valueOf(someData.size()), 5, RoundingMode.HALF_UP);
        sum = BigDecimal.ZERO;
        for (BigDecimal data : someData) {
            sum = sum.add(data.subtract(ave).pow(2));
        }
        Log.d("TAG", "sum=" + sum);
        return sqrt(sum.divide(BigDecimal.valueOf(someData.size() - 1), RoundingMode.HALF_UP));
    }

    /**
     * BigDecimal开方运算.
     *
     * @param num 被开方的BigDecimal对象.
     * @return 以四舍五入方式修约的精确度0.01的开方结果
     */
    private BigDecimal sqrt(BigDecimal num) {
        /*BigDecimal result = num, pow = BigDecimal.valueOf(2);
        int count = 0, precision = 100;
        MathContext mc = new MathContext(precision, RoundingMode.HALF_UP);
        while (count < precision) {//迭代100次
            result = (result.add(num.divide(result, mc))).divide(pow, mc);
            count++;
        }
        result = result.setScale(2, BigDecimal.ROUND_HALF_UP);*/
        return BigDecimal.valueOf(Math.sqrt(num.doubleValue()));
    }

    /**
     * 对平均数进行修约.
     *
     * @return 修约后的平均数
     */
    private String getAmendAve(String u) {
        int xTag;
        if ((xTag = u.indexOf('x')) >= 0)
            u = u.substring(0, xTag - 1);
        String aveStr = getSrcData(ave.toString());
        int uPointLoc = u.indexOf('.'), avePointLoc = aveStr.indexOf('.');
        if (uPointLoc < 0) {
            String tempAve = String.valueOf(Integer.parseInt(aveStr.substring(0, 1)) + 1);
            if (avePointLoc < 0) {// u无点  ave无点
                if (ave.compareTo(BigDecimal.TEN) < 0)// 0 <= ave < 10
                    return aveStr;
                char baseNum = aveStr.charAt(1);
                String resultOne = aveStr.charAt(0) + " x 10^" + (aveStr.length() - 1);
                if (baseNum < '5') {
                    if (aveStr.length() - 1 > 1)
                        return resultOne;
                    return aveStr.charAt(0) + " x 10";
                } else {
                    if (baseNum == '5') {
                        for (int i = 2; i < aveStr.length(); i++) {
                            if (aveStr.charAt(i) > '0') {
                                if (tempAve.length() > 1)// 9 + 1 = 10
                                    return "1 x 10^" + aveStr.length();
                                return tempAve + " x 10^" + (aveStr.length() - 1);
                            }
                        }
                        if (aveStr.length() - 1 > 1)
                            return resultOne;
                        return aveStr.charAt(0) + " x 10";
                    } else {
                        if (tempAve.length() > 1)// 9 + 1 = 10
                            return "1 x 10^" + aveStr.length();
                        return tempAve + " x 10^" + (aveStr.length() - 1);
                    }
                }
            } else {// u无点  ave有点
                if (ave.compareTo(BigDecimal.valueOf(9.5)) <= 0) {// ave <= 9.5
                    BigDecimal aveMid = new BigDecimal("0" + aveStr.substring(avePointLoc));
                    if (aveMid.compareTo(BigDecimal.valueOf(0.5)) <= 0)// 小数部分在(0, 0.5}
                        return String.valueOf(aveStr.charAt(0));
                    else
                        return String.valueOf((char) (aveStr.charAt(0) + 1));
                } else if (ave.compareTo(BigDecimal.TEN) < 0) {// 9.5 < ave < 10
                    return "1 x 10";
                } else {// ave >= 10
                    char baseNum = aveStr.charAt(1);
                    if (baseNum < '5') {
                        if (avePointLoc - 1 > 1)
                            return aveStr.charAt(0) + " x 10^" + (avePointLoc - 1);
                        return aveStr.charAt(0) + " x 10";
                    } else {// 5后一定不是全零
                        if (tempAve.length() > 1)
                            return "1 x 10^" + avePointLoc;
                        return tempAve + " x 10^" + (avePointLoc - 1);
                    }
                }
            }
        } else {
            if (avePointLoc < 0) {// u有点  ave无点
                StringBuilder zeroStr = new StringBuilder(".");
                for (int i = uPointLoc + 1; i < u.length(); i++) {
                    zeroStr.append('0');
                }
                return aveStr + zeroStr;
            } else {// u有点  ave有点
                int uAfterPoint = u.length() - 1 - uPointLoc, aveAfterPoint = aveStr.length() - 1 - avePointLoc;
                if (uAfterPoint == aveAfterPoint)
                    return aveStr;
                else if (uAfterPoint > aveAfterPoint) {
                    StringBuilder zeroStr = new StringBuilder();
                    for (int i = aveAfterPoint - 1; i < uAfterPoint; i++) {
                        zeroStr.append('0');
                    }
                    return aveStr + zeroStr;
                } else {
                    int indexFlag = avePointLoc + uAfterPoint + 1;
                    char baseNum = aveStr.charAt(indexFlag);
                    if (baseNum < '5') {
                        return aveStr.substring(0, indexFlag);
                    } else {
                        BigDecimal decimal = new BigDecimal(aveStr.substring(0, indexFlag)).add(BigDecimal.valueOf(0.1).pow(uAfterPoint));
                        if (baseNum > '5') {
                            return getSrcData(decimal.toString());
                        } else {
                            for (int i = indexFlag + 1; i < aveStr.length(); i++) {
                                if (aveStr.charAt(i) > '0') {
                                    return getSrcData(decimal.toString());
                                }
                            }
                            return aveStr.substring(0, indexFlag);
                        }
                    }
                }
            }
        }
    }

    private String amendResultU(String uResult) {
        String tempU;
        try {
            tempU = new BigDecimal(uResult).toString();
        } catch (NumberFormatException e) {
            return uResult;
        }
        if (tempU.indexOf('E') >= 0)
            return tempU.replace("E", " x 10^");
        else if (tempU.indexOf('e') >= 0)
            return tempU.replace("e", "x 10^");
        return tempU;
    }

    String getResult() {
        String uResult = getU();
        return getAmendAve(uResult) + " +- " + amendResultU(uResult);
    }
}
