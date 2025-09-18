# _*_ coding: utf-8 _*_

import sys
import os
import shutil

dir_proto = '.'
dir_protobuf_output = './Protobuf/Lua'
dir_protobuf_client = '../Resource_Client/Res/lua/protobuf'

def run_system_cmd(cmd_str,err_str=""):
	print(cmd_str)
	result = os.system(cmd_str)
	if result !=0:
		print(result, err_str)
		raise Exception

def svn_cleanup_proto():
	svn_cleanup_cmd = 'svn cleanup --remove-unversioned --remove-ignored %s' % dir_proto
	outputStr = os.popen(svn_cleanup_cmd).read()
	print(outputStr)

def svn_update_proto():
	svn_cleanup_proto()

	svn_up_cmd = 'svn up %s' % dir_proto
	outputStr = os.popen(svn_up_cmd).read()

	if not outputStr:
		exit_with_error('fail to svn update proto')

def generate_protobuf(proto_file_str):
	if proto_file_str == '*':
		gen_cmd = 'call ./BuildProto-lua.bat'
		run_system_cmd(gen_cmd)
		return

	gen_cmd = 'call ./BuildProto-lua-signal_with_input.bat %s'
	for proto_file in proto_file_str.split(','):
		run_system_cmd(gen_cmd % proto_file)

def svn_cleanup_client():
	svn_cleanup_cmd = 'svn cleanup --remove-unversioned --remove-ignored %s' % dir_protobuf_client
	outputStr = os.popen(svn_cleanup_cmd).read()
	print(outputStr)

def svn_update_client():
	svn_cleanup_client()

	svn_up_cmd = 'svn up %s' % dir_protobuf_client
	outputStr = os.popen(svn_up_cmd).read()

	if not outputStr:
		exit_with_error('fail to svn update proto')

def copy_output_protobuf():
	svn_update_client()

	has_copy = False
	for lua_file in os.listdir(dir_protobuf_output):
		fullpath = os.path.join(dir_protobuf_output, lua_file)
		if os.path.isfile(fullpath):
			print('>>copy file: %s --> %s' % (fullpath, dir_protobuf_client))
			shutil.copy(fullpath, dir_protobuf_client)
			has_copy = True
	if not has_copy:
		exit_with_error('no file copied')

def svn_ci_protobuf(svn_no):
	svn_st_cmd = 'svn st %s' % dir_protobuf_client
	outputStr = os.popen(svn_st_cmd).read()

	if not outputStr:
		print('protobuf all updated, nothing to do!')
		exit(0)

	svn_ci_log = '[jenkins] update from jenkins'
	if svn_no and svn_no != '0':
		svn_ci_log += ' [after commit revision: %s]' % svn_no
		
	svn_add_cmd = 'svn add --force %s ' % (dir_protobuf_client)
	addResult = os.popen(svn_add_cmd).read()
	print(addResult)

	svn_ci_cmd = 'svn ci %s -m"%s"' % (dir_protobuf_client, svn_ci_log)
	outputStr = os.popen(svn_ci_cmd).read()

	print(outputStr)
	if not outputStr or outputStr.find('Committed revision') <= 0:
		exit_with_error('fail to svn commit protobuf')

def exit_with_error(errStr):
	print('\nError: %s\n' % errStr)
	exit(1)

if __name__ == '__main__':
	proto_file_str = sys.argv[1]
	svn_no = sys.argv[2]
	if not proto_file_str:
		exit_with_error('no proto file provided!')

	svn_update_proto()

	generate_protobuf(proto_file_str)

	copy_output_protobuf()

	svn_ci_protobuf(svn_no)

	print('Well Done!')