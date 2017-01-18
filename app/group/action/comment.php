<?php 
defined('IN_TS') or die('Access Denied.');
//用户是否登录
$userid = aac('user')->isLogin();
switch($ts){
	//添加评论
	case "do":
		
		$authcode = strtolower($_POST['authcode']);
		
		if ($TS_SITE ['isauthcode']) {
			if ($authcode != $_SESSION ['verify']) {
				tsNotice ( "验证码输入有误，请重新输入！" );
			}
		}
		
		$topicid	= intval($_POST['topicid']);
		$content	= tsClean($_POST['content']);

        $ispublic = intval($_POST['ispublic']);
		
		
		
		//过滤内容开始
		if($TS_USER['isadmin']==0){
			aac('system')->antiWord($content);
		}
		//过滤内容结束
		
		if($content==''){
			tsNotice('没有任何内容是不允许你通过滴^_^');
		}else{
			$commentid = $new['group']->create('group_topic_comment',array(
				'topicid'	=> $topicid,
				'userid'	=> $userid,
				'content'	=> $content,
                'ispublic'=>$ispublic,
				'addtime'=> time(),
			));
			
			//统计评论数
			$count_comment = $new['group']->findCount('group_topic_comment',array(
				'topicid'=>$topicid,
			));
			
			//更新帖子最后回应时间和评论数			
			$new['group']->update('group_topic',array(
				'topicid'=>$topicid,
			),array(
				'count_comment'=>$count_comment,
				'uptime'=>time(),
			));
			
			//对积分进行处理
			aac('user')->doScore($GLOBALS['TS_URL']['app'], $GLOBALS['TS_URL']['ac'], $GLOBALS['TS_URL']['ts']);
			
			//发送系统消息(通知楼主有人回复他的帖子啦)			
			$strTopic = $new['group']->find('group_topic',array(
				'topicid'=>$topicid,
			));
			
			if($strTopic['userid'] != $TS_USER['userid']){
			
				$msg_userid = '0';
				$msg_touserid = $strTopic['userid'];
				$msg_content = '你的帖子：《'.$strTopic['title'].'》新增一条评论，快去看看给个回复吧^_^ ';
                $msg_tourl = tsUrl('group','topic',array('id'=>$topicid));
				aac('message')->sendmsg($msg_userid,$msg_touserid,$msg_content,$msg_tourl);
				
			}
			
			

			header("Location: ".tsUrl('group','topic',array('id'=>$topicid)));
		}	
	
		break;
		
	//删除评论
	case "delete":
		
		$commentid = intval($_GET['commentid']);
		
		$strComment = $new['group']->find('group_topic_comment',array(
			'commentid'=>$commentid,
		));
		
		$strTopic = $new['group']->find('group_topic',array(
			'topicid'=>$strComment['topicid'],
		));
		
		$strGroup = $new['group']->find('group',array(
			'groupid'=>$strTopic['groupid'],
		));
		
		if($strTopic['userid']==$userid || $strGroup['userid']==$userid || $TS_USER['isadmin']==1 || $strComment['userid']==$userid){
			
			$new['group']->delComment($commentid);
			
			//统计评论数
			$count_comment = $new['group']->findCount('group_topic_comment',array(
				'topicid'=>$strTopic['topicid'],
			));
			
			//更新帖子最后回应时间和评论数			
			$new['group']->update('group_topic',array(
				'topicid'=>$strTopic['topicid'],
			),array(
				'count_comment'=>$count_comment,
			));

            //处理积分
            aac('user')->doScore($GLOBALS['TS_URL']['app'], $GLOBALS['TS_URL']['ac'], $GLOBALS['TS_URL']['ts'],$strComment['userid']);
			
			
			
		}
		
		//跳转回到帖子页
		header("Location: ".tsUrl('group','topic',array('id'=>$strComment['topicid'])));
		
		break;
}