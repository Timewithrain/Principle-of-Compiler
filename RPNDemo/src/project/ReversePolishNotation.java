package project;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class ReversePolishNotation {

    /**
     * stack 符号栈
     * output 保存生成逆波兰表达式
     * priority 运算符优先级表，用于判断符号栈顶符号和当前符号的优先级
     */
    static Stack<Character> stack = new Stack<>();
    static Stack<Integer> calculateStack = new Stack<>();
    static StringBuffer output = new StringBuffer();
    static Map<Character,Integer> priority = new HashMap<>();
    static Map<Character,Integer> inputDigit = new HashMap<>();

    /**
     * 初始化算术运算符的优先级，优先级数值越小优先级越高
     * '-'号有两种优先级，当其单独存在时为取负优先级为2，当其前一个运算符不为左括号时则为减号优先级为4
     * 由于左右括号不作为运算符因此将其优先级置为最低
     */
    static void init(){
        priority.put('(',1);
        priority.put(')',1);
        priority.put('*',3);
        priority.put('/',3);
        priority.put('+',4);
        priority.put('-',4);

    }

    static String getRPN(String input){
        for (int i=0;i<input.length();i++){
            char code = input.charAt(i);
            //扫描到最后一个字符时停止扫描
            if (code=='#'){
                break;
            }
            System.out.printf("%-10c\t%10s\t\t\t\t%-10s\t\t\t%-10s\n",code,input.substring(i),getStack(stack),output);
            //判断扫描的字符是否为数字，若是数字则将其放入加入输出串并继续扫描
            if(Character.isDigit(code)){
                output.append(code);
                continue;
            }
            //判断当前扫描的字符是否为字母，若不是字母则为运算符
            if (Character.isLetter(code)){
                if (code!='('&&code!=')'){
                    output.append(code);
                }
            }else {
                //若当前扫描字符为运算符则将其入栈
                if (stack.isEmpty()){
                    //符号栈为空直接入栈
                    stack.push(code);
                }else if (code==')') {
                    //当遇到右括号直接将符号栈的所有符号出栈
                    while(!stack.isEmpty()){
                        output.append(stack.pop());
                    }
                }else {
                    //获取栈顶元素
                    char stackTop = stack.lastElement();
                    //若当前运算符优先级高于栈顶运算符(数值小优先级给高)，则将其入栈
                    int a = priority.get(code);
                    //若当前运算符为-时特殊处理，需判断其前一个符号是否为'(',若是则'-'为取负，提升其优先级，否则为减，不做处理
                    if (code=='-'&&input.charAt(i-1)=='('){
                        a = 2;
                    }
                    int b = priority.get(stackTop);
                    if (a<b){
                        stack.push(code);
                    }else {
                        //若当前运算符优先级低于栈顶运算符则将栈顶运算符出栈，再将当前运算符入栈
                        char c =  stack.pop();
                        if (c!='('){
                            output.append(c);
                        }
                        stack.push(code);
                    }
                }
            }

        }
        //若运算完成后运算符栈内还存有运算符则将其添加至output中
        while (!stack.isEmpty()){
            output.append(stack.pop());
        }
        return output.toString();
    }

    static String getStack(Stack<Character> s){
        StringBuffer sb = new StringBuffer();
        for (Character c : s){
            sb.append(c);
        }
        return sb.toString();
    }

    static String getIntegerStack(Stack<Integer> s){
        StringBuffer sb = new StringBuffer();
        for (Integer c : s){
            sb.append(c+",");
        }
        return sb.toString();
    }

    static void initInputDigit(int ...num){
        for (int i=0;i<num.length&&i<26;i++){
            inputDigit.put((char) (i+'a'),num[i]);
        }
    }

    //判断字符是否为运算符
    static boolean isOperator(Character c){
        boolean isAOperator = false;
        if (c=='+'||c=='-'||c=='*'||c=='/'||c=='('||c==')'){
            isAOperator = true;
        }
        return isAOperator;
    }

    static int calculate(String rpn){
        for (int i=0;i<rpn.length();i++){
            char code = rpn.charAt(i);
            System.out.printf("%-10c\t%10s\t\t\t\t%-10s\n",code,rpn.substring(i),getIntegerStack(calculateStack));
            //若读取字符不为操作符则将其解析并入栈，否则进行运算
            if (!isOperator(code)){
                int num = 0;
                //若读取字符为数字，则读取至数字结束并将其入栈
                if (Character.isDigit(code)){
                    num = code - '0';
                    //读取数字至结尾
                    for (int j=i+1;j<rpn.length();j++){
                        code = rpn.charAt(j);
                        if (Character.isDigit(code)){
                            num = num * 10;
                            num = num + (code - '0');
                        }else {
                            i = j - 1;
                            break;
                        }
                    }
                }else {
                    //若不是数字则为字母，将其转换为数字
                    num = inputDigit.get(code);
                }
                calculateStack.push(num);
            }else{
                int aNum = calculateStack.pop();
                int bNum = calculateStack.pop();
                int result = 0;
                switch (code){
                    case '+':
                        result = bNum + aNum;
                        break;
                    case '-':
                        result = bNum - aNum;
                        break;
                    case '*':
                        result = bNum * aNum;
                        break;
                    case '/':
                        result = bNum / aNum;
                        break;
                }
                calculateStack.push(result);
            }
        }
        return calculateStack.pop();
    }

    public static void main(String[] args) {
        //构造逆波兰表达式
        init();
//        String input = "(a+b*c)*d#";
        String input = "(a+a*c)*a-2*a#";
        String rpn = getRPN(input);
        System.out.println("逆波兰式："+rpn);

        //根据逆波兰表达式进行数值计算
        initInputDigit(1,2,3);
        int result = calculate(rpn);
        System.out.println("结果为:" + result);
    }

}
