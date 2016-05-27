[#--
My Macro does great things. For example, it says "Hello".
@param you optional parameter to direct the "Hello" to - defaults to "world!".
--]
[#macro mymacro you="world!"]
Hello, ${you}
[/#macro]

[#if (_testing!false)]
[@test_self /]
[/#if]

[#--
Test this library
--]
[#macro test_self]
 [@mymacro /]
[/#macro]
