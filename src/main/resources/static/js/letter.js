$(function(){
	$("#sendBtn").click(send_letter);/*发送按钮触发send_letter函数*/
	$(".close").click(delete_msg);/*取消按钮触发delete_msg函数*/
});

function send_letter() {
	$("#sendModal").modal("hide");/*隐藏对话框*/
	/*===============分割线=============*/
	var toName = $("#recipient-name").val();
	var content = $("#message-text").val();
	$.post(
		CONTEXT_PATH + "/letter/send",
		{"toName":toName,"content":content},
		function (data) {
			data = $.parseJSON(data);
			if(data.code == 0) {
				$("#hintBody").text("发送成功！");
			}else {
				$("#hintBody").text(data.msg);
			}
			$("#hintModal").modal("show");/*显示提示框*/
			setTimeout(function(){/*2s后关闭提示框并刷新当前页面*/
				$("#hintModal").modal("hide");
				location.reload();/*刷新当前页面*/
			}, 2000);
		}
	);

	/*===============分割线=============*/
	/*$("#hintModal").modal("show");/!*显示提示框*!/
	setTimeout(function(){/!*2s后关闭提示框*!/
		$("#hintModal").modal("hide");
	}, 2000);*/
}

function delete_msg() {
	// TODO 删除数据
	$(this).parents(".media").remove();
}