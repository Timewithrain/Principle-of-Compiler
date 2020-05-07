package analysis;

import java.util.*;

public class LLAnalysis {

    static int pointer;
    static String input;
    static Set<String> endCode = new HashSet<String>();
    static Set<String> notEndCode = new HashSet<String>();
    static Map rules = new HashMap<String,String[]>();
    static Map firstSet = new HashMap<String,HashSet<String>>();
    static Map followSet = new HashMap<String,HashSet<String>>();
    static Map selectSet = new HashMap<String,HashSet<String>>();
    static Map table = new HashMap<String,HashMap<String,String>>();
    static Stack analyzeStack = new Stack<Character>();

    /**
     * 初始化所有的生成式
     */
    static void initRules(){
        rules.put("E", new String[]{"TG"});
        rules.put("G", new String[]{"+TG", "-TG","ε"});
        rules.put("T", new String[]{"FS"});
        rules.put("S", new String[]{"*FS","/FS","ε"});
        rules.put("F", new String[]{"i","(E)"});
        //初始化非终结符集合
        for (Object key : rules.keySet()){
            notEndCode.add(key.toString());
        }
        //初始化终结符集合
        for (Object key : rules.keySet()){
            String[] ruleArray = (String[]) rules.get(key);
            for (String rule : ruleArray){
                for (int i=0;i<rule.length();i++){
                    char code = rule.charAt(i);
                    if (!Character.isUpperCase(code)&&code!='ε'){
                        endCode.add(rule.charAt(i)+"");
                    }
                }
            }
        }
    }

//    static void initSet(Map<String, HashSet<String>> set){
//        set.put("E",new HashSet<String>());
//        set.put("G",new HashSet<String>());
//        set.put("T",new HashSet<String>());
//        set.put("S",new HashSet<String>());
//        set.put("F",new HashSet<String>());
//    }

    static Map<String, HashSet<String>> initSet(Map<String, HashSet<String>> set,Set<String> codes){
        for (Object obj : codes){
            set.put(obj.toString(),new HashSet<String>());
        }
        return set;
    }

    static Set<String> initFirstSet(String key){
        String[] ruleArray = (String[]) rules.get(key);
        Set set = (HashSet<String>)firstSet.get(key);
        Set<String> returnSet = null;
        for (String str : ruleArray){
            char code = str.charAt(0);
            //若生成式的右部首字符为非终结符则递归获取其终结符，将终结符存入first集中
            if (isNotEndCode(code)){
                returnSet = initFirstSet(code+"");
                for (Object obj : returnSet){
                    set.add(obj);
                }
            }else {
                //若生成式的右部首字符为终结符，则将其加入至非终结符对应的First集合，并将其放入将返回至上一层的first集合中
                set.add(code);
                if (returnSet==null){
                    returnSet = new HashSet<String>();
                }
                returnSet.add(code+"");
            }
        }
        return returnSet;
    }

    static Boolean initFollowSet(){
        int setLen = 0;
        Boolean followSetRefresh = false;
        for (Object entry : rules.entrySet()){
            String key = ((Map.Entry<String,String[]>)entry).getKey();
            String[] ruleArray = (String[]) rules.get(key);
            for (String str:ruleArray){
                int len = str.length();
                //遍历生成式右部的每个字符，获取所有非终结符的Follow集
                for (int i=0;i<len;i++){
                    //若生成式右部的字符为非终结符则获取其Follow集，否则跳过
                    char code = str.charAt(i);
                    if (isNotEndCode(code)){
                        Set set = (HashSet<String>)followSet.get(code+"");
                        setLen = set.size();
                        //若此非终结符后有元素且也为非终结符
                        if (i<len-1&&isNotEndCode(str.charAt(i+1))){
                            char nextCode = str.charAt(i+1);
                            Set nextFirstSet = (HashSet<String>) firstSet.get(nextCode+"");
                            //将此非终结符的下一个非终结符的First集中的终结符加入到此终结符的Follow集
                            for (Object obj:nextFirstSet){
                                if (!obj.equals('ε')){
                                    set.add(obj);
                                }
                            }
                            //若此此非终结符的下一个非终结符可为空，则将此生成式的原非终结符的Follow集加入此终结符的Follow集
                            if (nextFirstSet.contains('ε')){
                                Set rootFollow = (HashSet<String>)followSet.get(key+"");
                                for (Object obj:rootFollow){
                                    set.add(obj);
                                }
                            }
                        }
                        //若此非终结符后有元素且也为终结符
                        if(i<len-1&&!isNotEndCode(str.charAt(i+1))){
                            //将非终结符之后的终结符加入Follow集
                            String nextCode = str.charAt(i+1) + "";
                            if (!nextCode.equals('ε')){
                                set.add(nextCode);
                            }
                        }
                        //若此非终结符为最后一个元素
                        if (i==len-1){
                            //将此生成式的原非终结符的Follow集加入此终结符的Follow集
                            Set rootFollow = (HashSet<String>)followSet.get(key+"");
                            for (Object obj:rootFollow){
                                set.add(obj);
                            }
                        }
                        //判断follow集是否被修改
                        if (setLen!=set.size()){
                            followSetRefresh = true;
                        }
                    }
                }
            }
        }
        return followSetRefresh;
    }

    static void initSelectSet(){
        for (Object entry : rules.entrySet()){
            String key = ((Map.Entry<String,String[]>)entry).getKey();
            String[] ruleArray = (String[]) rules.get(key);
            for (String ruleRight : ruleArray){
                selectSet.put(key+"->"+ruleRight,new HashSet<String>());
            }
        }
        for (Object entry : rules.entrySet()){
            String key = ((Map.Entry<String,String[]>)entry).getKey();
            String[] ruleArray = (String[]) rules.get(key);
            for (String rule : ruleArray){
                char code = rule.charAt(0);
                //若生成式的右部首字符为非终结符则将其first集中的终结符加入生成式的Select集
                if (isNotEndCode(code)){
                    Set<String> set = (Set<String>) firstSet.get(code+"");
                    Set<String> selectRightPart = (Set<String>) selectSet.get(key+"->"+rule);
                    for (Object obj : set){
                        if (!obj.equals('ε')){
                            selectRightPart.add(obj.toString());
                        }
                    }
                }else if ("ε".equals(code+"")){
                    //若生成式的右部为空，则将生成式左部非终结符的Follow集加入生成式的Select集
                    Set<String> set = (Set<String>) followSet.get(key);
                    Set<String> selectRightPart = (HashSet<String>) selectSet.get(key+"->"+rule);
                    for (Object obj : set){
                        selectRightPart.add(obj.toString());
                    }
                }else {
                    //若生成式的右部首字符不为非终结符也不为空则一定为终结符，则直接将其加入入生成式的Select集
                    Set<String> selectRightPart = (Set<String>) selectSet.get(key+"->"+rule);
                    selectRightPart.add(code+"");
                }
            }
        }
    }

    //初始化预测分析表
    static void initPredictTable(){
        //此处nECode为预测表中的非终结符,其作为纵坐标
        for (Object nECode : notEndCode){
            Map<String,String> map = (Map<String, String>) table.get(nECode);
            if (map==null){
                map = new HashMap<String, String>();
            }
            //将非终结符与Select集中的生成式首字母做匹配若匹配成功则，将生成式对应的终结符与
            Set<String> ruleArray = selectSet.keySet();
            for (String rule : ruleArray){
                if (nECode.equals(rule.charAt(0)+"")){
                    Set<String> set = (Set<String>) selectSet.get(rule);
                    for (Object eCode:set){
                        map.put(eCode.toString(),rule);
                    }
                }
            }
            table.put(nECode,map);
        }
    }

    static void init(){
        //初始化所有的生成式
        initRules();
        //初始化非终结符的First集
        initSet(firstSet,notEndCode);
        for (Object entry : rules.entrySet()){
            initFirstSet(((Map.Entry<String,String[]>)entry).getKey());
        }
        //初始化非终结符的Follow集
        initSet(followSet,notEndCode);
        Boolean isFollowRefresh = true;
        //当监测到任意非终结符的Follow集修改则再次进行Follow集的生成
        while (isFollowRefresh){
            isFollowRefresh = initFollowSet();
        }
        initFollowSet();
        //初始化生成式的Select集
        initSelectSet();
        //初始化分析预测表
        initPredictTable();
    }

//    static Map table = new HashMap<String,HashMap<String,String>>();
    static void analyze(char code){
        //判断输入字符c是否为非终结符
        while(input.charAt(pointer)!='#'){
            if (isNotEndCode(code)){
                Map<String,String> row = (Map<String, String>) table.get(code+"");
                String rule = row.get(input.charAt(pointer)+"");
                String str = rule.substring(3);
                pushString(str);
                printStack(analyzeStack,rule);
                code = (Character) analyzeStack.pop();
            }else if (code!='ε'){
                if (code==input.charAt(pointer)){
                    code = (Character) analyzeStack.pop();
                    pointer++;
                    printStack(analyzeStack,null);
                }
            }else {
                //当生成式右端为空时继续执行
                printStack(analyzeStack,null);
                code = (Character) analyzeStack.pop();
            }
        }
    }

    /**
     * 判断函数输入字符是否为非终结符(此处以大写字母作为非终结符)，若是返回true否则返回false
     * @prem Character
     * @return Boolean
     */
    static Boolean isNotEndCode(Character c){
        Boolean isEndCode = false;
        if (Character.isUpperCase(c)){
            isEndCode = true;
        }
        return isEndCode;
    }

    /**
     * 将字符串逐字符压入分析栈
     * @param str
     */
    static void pushString(String str){
        for (int i=str.length()-1;i>=0;i--){
            analyzeStack.push(str.charAt(i));
        }
    }

    static void printSet(Map s){
        for (Object obj : s.entrySet()){
            Map.Entry<String,HashSet<String>> entry = (Map.Entry<String,HashSet<String>>)obj;
            Set<String> set = entry.getValue();
            System.out.printf("%s:\t\t",entry.getKey());
            for (Object str : set){
                System.out.printf("%s ",str);
            }
            System.out.println();
        }
    }

    static void printTable(){
        for (String nECode : notEndCode){
            Map<String,String> row = (Map) table.get(nECode);
            System.out.printf("%s:",nECode);
            for (String eCode : endCode){
                String str = row.get(eCode);
                if (str!=null){
                    System.out.printf("[%s]%s\t\t",eCode,str);
                }
            }
            System.out.println();
        }
    }

    static void printStack(Stack<Character> stack,String rule){
        StringBuffer sb = new StringBuffer();
        for (Character c : stack){
            sb.append(c);
        }
        if (rule!=null){
            System.out.printf("%-10s\t%10s\t\t\t\t%-10s",sb,input.substring(pointer),rule);
        }else {
            System.out.printf("%-10s\t%10s",sb,input.substring(pointer));
        }
        System.out.println();
    }

    public static void main(String[] args) {

        init();
        System.out.println("=======非终结符=======");
        for (String str : notEndCode){
            System.out.printf("%s ",str);
        }
        System.out.println("\n=======终结符=======");
        for (String str : endCode){
            System.out.printf("%s ",str);
        }
        System.out.println();
        System.out.println("=======First集合=======");
        printSet(firstSet);
        System.out.println("=======Follow集合=======");
        printSet(followSet);
        System.out.println("=======Select集合=======");
        printSet(selectSet);
        System.out.println("=======预测分析表=======");
        printTable();

        pointer = 0;
        //根据文法将E作为语法树根
        analyzeStack.push('#');
        analyzeStack.push('E');
        //输入符号串
        input = "i+i*i#";
        char code = (char) analyzeStack.pop();
        System.out.println("==================================");
        System.out.println("分析栈\t\t\t剩余输入串\t\t\t产生式");
        analyze(code);


    }

}
