package yuan.ssm.common.status;

/**
 * ==================================================
 * <p/>
 * 版权：　软件工程.net12-1 原明卓
 * <p/>
 * 项目：　基于用户兴趣标签的新闻推荐系统
 * <p/>
 * 作者：　原明卓
 * <p/>
 * 版本：　1.0
 * <p/>
 * 创建日期：　16-4-14 上午11:24
 * <p/>
 * 功能描述： 修改信息状态
 * <p>
 * <p/>
 * 功能更新历史：
 * <p>
 * ==================================================
 */
public interface UpdateStatus {

    Integer UPDATE_SUCCESS=1; //修改成功

    Integer UPDATE_FAIL=-1; //修改失败

    Integer UPDATE_ID_ZORE=-2; //id小于等于0

}
