package com.nowcoder.community.entity;

/**
 * 封装分页信息
 */
public class Page {
    //当前页
    private int current = 1;
    //显示上限
    private int limit = 10;
    //总记录数
    private int rows;
    //查询路径（用于复用分页链接）
    private String path;

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if (current >= 1) {
            this.current = current;
        }
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if (limit>=1&&limit<=100){
            this.limit = limit;
        }
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        if (rows>=0){
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
     * 获取起始行数
     * @return
     */
    public int getOffset() {
        return (current-1)*limit;
    }

    /**
     * 获取总页数
     * @return
     */
    public int getTotal() {
        return rows%limit==0?rows/limit:rows/limit+1;
    }

    /**
     * 获取起始页码
     * @return
     */
    public int getStartPage() {
        int start = current-2;
        if (start<=0){
            start = 1;
        }
        return start;
    }
    /**
     * 获取结束页码
     * @return
     */
    public int getEndPage() {
        int end = current+2;
        if (end> getTotal()){
            end = getTotal();
        }
        return end;
    }

}
