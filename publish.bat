@rem һ��������ύ������������svnĿ¼
@rem �˽ű�������´��룬Ҳ�����Engine�е���Ŀ���д��������ֱ��ʹ��Engine/Publish�ļ����µ�jar��������Ŀ����
@rem ���ж�Engine����Ŀ��������޸ģ���������Engine�е�build.bat���д����Ȼ�������д˽ű�
set PUBLISH_DIR=Publish
set SVN_XML_DIR=xmlconfig

svn checkout -q svn://10.0.1.254/redalert/Version_Server/trunk/server-start %PUBLISH_DIR%
call gradle clean
call gradle jar
@rem ��������-DversionFolder=windows�� windows �ַ���Ϊ��汾����ѡ���������Ӧ���ļ�Ŀ¼��Launcher�ļ����µİ汾�ļ������ƣ�Ĭ���ļ�������Ϊ windows
call gradle -DversionFolder=windows publish

if exist %SVN_XML_DIR% (
	rd /s /q %SVN_XML_DIR%
)
svn checkout -q svn://10.0.1.254/redalert/Resource_Product/trunk/0.���ñ�/2.��̨ %SVN_XML_DIR%
xcopy /e /y /q %SVN_XML_DIR%\xml %PUBLISH_DIR%\xml\
xcopy /e /y /q %SVN_XML_DIR%\activity %PUBLISH_DIR%\activity\
echo commit SVN
cd %PUBLISH_DIR%
svn add * --force
svn commit -m �������Զ�����ύ
cd ..
echo delete publish dir
rd /s /q %PUBLISH_DIR%
rd /s /q %SVN_XML_DIR%
echo =====================================
echo ============build success============
echo =====================================
pause