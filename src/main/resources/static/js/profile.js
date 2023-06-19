$(function(){
	$(".follow-btn").click(follow);
});

function follow() {
	var btn = this;
	/*这里为了方便，根据按钮的样式，确定发送那种ajax请求。*/
	if($(btn).hasClass("btn-info")) {
		// 关注TA
		$.post(
			CONTEXT_PATH + "/follow",
			{"entityType":3, "entityId":$(btn).prev().val()},/*这里取button的上一个节点值获得数据*/
			function (data) {
				data = $.parseJSON(data);
				if(data.code == 0) {
					window.location.reload();//这里为了简化前端，直接刷新页面，正确的做法是刷新样式。
				} else {
					alert(data.msg);
				}
			}
		);
		// $(btn).text("已关注").removeClass("btn-info").addClass("btn-secondary");
	} else {
		// 取消关注
		// $(btn).text("关注TA").removeClass("btn-secondary").addClass("btn-info");
		$.post(
			CONTEXT_PATH + "/unfollow",
			{"entityType":3, "entityId":$(btn).prev().val()},/*这里取button的上一个节点值获得数据*/
			function (data) {
				data = $.parseJSON(data);
				if(data.code == 0) {
					window.location.reload();//这里为了简化前端，直接刷新页面，正确的做法是刷新样式。
				} else {
					alert(data.msg);
				}
			}
		);
	}
}