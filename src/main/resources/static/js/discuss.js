//注意：下面的写法都不是原生js写法，而是基于jQuery库。
//而jQuery有被淘汰的大趋势，所以这里了解即可。

//页面加载后，调用的函数，该函数表示点击按钮时调用对应js方法
$(function () {
    $("#topBtn").click(setTop);
    $("#wonderfulBtn").click(setWonderful);
    $("#deleteBtn").click(setDelete);
})

// 点赞 处理用户的点赞操作，并根据返回的数据更新前端显示。
function like(btn, entityType, entityId, entityUserId, postId) {
    /*使用jQuery的$.post方法发送异步POST请求。*/
    $.post(
        CONTEXT_PATH + "/like",
        {"entityType":entityType, "entityId":entityId, "entityUserId": entityUserId, "postId":postId},
        /*当请求成功后，执行回调函数处理返回的数据。*/
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

// 置顶
function setTop() {
    $.post(
        CONTEXT_PATH + "/discuss/top",
        {"id":$("#postId").val()},
        function (data) {
            data = $.parseJSON(data);
            if(data.code == 0) {
                $("#topBtn").text(data.type==1?'取消置顶':'置顶');/*显示置顶或取消置顶*/
            } else {
                alert(data.msg);
            }
        }
    );
}

// 加精
function setWonderful() {
    $.post(
        CONTEXT_PATH + "/discuss/wonderful",
        {"id":$("#postId").val()},
        function (data) {
            data = $.parseJSON(data);
            if(data.code == 0) {
                $("#wonderfulBtn").text(data.status==1?'取消加精':'加精');/*显示加精或取消加精*/
            } else {
                alert(data.msg);
            }
        }
    );
}

// 删除
function setDelete() {
    $.post(
        CONTEXT_PATH + "/discuss/delete",
        {"id":$("#postId").val()},
        function (data) {
            data = $.parseJSON(data);
            if(data.code == 0) {
                location.href = CONTEXT_PATH + "/index";/*重定向当前页面的url，即删除帖子后，页面跳转到首页*/
            } else {
                alert(data.msg);
            }
        }
    );
}
