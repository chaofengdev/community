package com.nowcoder.community.entity;

/*封装分页的相关信息*/
public class Page {
    // 当前页码
    private int current = 1;

    // 显示上限
    private int limit = 10;

    // 数据总数（用于计算总的页数）
    private int rows;

    // 查询路径（用于复用分页链接）
    private String path;

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        //为了代码的健壮性，需要在这里判断当前页码是否合法，如果为0或者负数，则页码不合法
        if(current >= 1) {
            this.current = current;
        }
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        //限制显示的上限是10-100条
        if(limit >= 1 && limit <= 100) {
            this.limit = limit;
        }
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        //总的页数必须大于等于0
        if(rows >= 0) {
            this.rows = rows;
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 获取当前页的起始行
     * 数据库查询的时候，需要用到起始行号而不是当前页，起始行号可以通过当前页算出
     * @return
     */
    public int getOffset() {
        // current * limit - limit
        // 当前页乘以每页条数，等于当前页最后一条记录的行号，减去当前页条数等于当前页的起始行号
        return (current - 1) * limit;
    }

    /**
     * 获取总的页数
     * @return
     */
    public int getTotal() {
        // rows / limit [+1]
        // 总页数等于总记录数除以每页记录数，不能整除就将结果加1
        if(rows % limit == 0) {
            return rows / limit;
        }else {
            return (rows / limit) + 1;//多出一页，存放多出的相关记录
        }
    }

    /**
     * 获取起始页码，注意不是起始行号，是前端展示的起始页码
     * @return
     */
    public int getFrom() {
        int from = current - 2;
        return from < 1 ? 1 : from;//如果是第一页，则起始页码也是第一页
    }

    /**
     * 获取结束页码，同上
     * @return
     */
    public int getTo() {
        int to = current + 2;
        int total = getTotal();//总页数
        return to > total ? total : to;//如果是最后一页，则结束页码也是最后一页
    }

    @Override
    public String toString() {
        return "Page{" +
                "current=" + current +
                ", limit=" + limit +
                ", rows=" + rows +
                ", path='" + path + '\'' +
                '}';
    }
}
