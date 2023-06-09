$(function(){
	$("#publishBtn").click(publish);
});

function publish() {
	//隐藏对话框
	$("#publishModal").modal("hide");

	//获取标题和内容
	var title = $("#recipient-name").val();
	var content = $("#message-text").val();
	//发送异步请求--post
	$.post(
		CONTEXT_PATH + "/discuss/add",
		{"title":title,"content":content},
		function (data) {
			data = $.parseJSON(data);
			//在提示框当中显示返回的消息
			$("#hintBody").text(data.msg);
			//显示提示框
			$("#hintModal").modal("show");
			//2秒后，自动隐藏提示框
			setTimeout(function(){
				$("#hintModal").modal("hide");
				//刷新页面
				if(data.code == 0) {
					window.location.reload();//重新加载当前页面，更好的方法是在前端进行增量更新。
				}
			}, 2000);
		}
	);

	//显示提示消息并过一段时间后隐藏
	$("#hintModal").modal("show");
	setTimeout(function(){
		$("#hintModal").modal("hide");
	}, 2000);
}