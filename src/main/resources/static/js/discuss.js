function like(btn, entityType, entityId, entityUserId, postId) {
    $.post(
        CONTEXT_PATH + "/like",
        {"entityType":entityType, "entityId":entityId, "entityUserId": entityUserId, "postId":postId},
        function (data) {
            data = $.parseJSON(data);
            if(data.code == 0) {//修改前端显示
                $(btn).children("i").text(data.likeCount);//子节点的文本显示
                $(btn).children("b").text(data.likeStatus==1?'已赞':"赞");//子节点的文本显示
            }else {
                alert(data.msg);
            }
        }
    );
}