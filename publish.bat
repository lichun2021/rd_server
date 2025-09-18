@rem 一键打包并提交服务器到测试svn目录
@rem 此脚本不会更新代码，也不会对Engine中的项目进行打包，而是直接使用Engine/Publish文件夹下的jar包进行项目依赖
@rem 若有多Engine中项目代码进行修改，请先运行Engine中的build.bat进行打包，然后再运行此脚本
set PUBLISH_DIR=Publish
set SVN_XML_DIR=xmlconfig

svn checkout -q svn://10.0.1.254/redalert/Version_Server/trunk/server-start %PUBLISH_DIR%
call gradle clean
call gradle jar
@rem 下面命令-DversionFolder=windows中 windows 字符串为多版本发布选择参数，对应本文件目录中Launcher文件夹下的版本文件夹名称，默认文件夹名称为 windows
call gradle -DversionFolder=windows publish

if exist %SVN_XML_DIR% (
	rd /s /q %SVN_XML_DIR%
)
svn checkout -q svn://10.0.1.254/redalert/Resource_Product/trunk/0.配置表/2.后台 %SVN_XML_DIR%
xcopy /e /y /q %SVN_XML_DIR%\xml %PUBLISH_DIR%\xml\
xcopy /e /y /q %SVN_XML_DIR%\activity %PUBLISH_DIR%\activity\
echo commit SVN
cd %PUBLISH_DIR%
svn add * --force
svn commit -m 服务器自动打包提交
cd ..
echo delete publish dir
rd /s /q %PUBLISH_DIR%
rd /s /q %SVN_XML_DIR%
echo =====================================
echo ============build success============
echo =====================================
pause