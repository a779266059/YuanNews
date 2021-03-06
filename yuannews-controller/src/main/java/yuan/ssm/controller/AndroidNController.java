package yuan.ssm.controller;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import yuan.ssm.common.util.FileTool;
import yuan.ssm.other.CommentJo;
import yuan.ssm.other.DataBean;
import yuan.ssm.other.PageVo;
import yuan.ssm.other.TuijianModel;
import yuan.ssm.pojo.CSCustom;
import yuan.ssm.pojo.NewsCustom;
import yuan.ssm.service.customer.NewsService;
import yuan.ssm.service.customer.UserService;
import yuan.ssm.service.mobile.UserAppService;
import yuan.ssm.vo.LikedVo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
 * 创建日期：　16-5-5 下午6:40
 * <p/>
 * 功能描述： 客户端接口-新闻，分类，来源，标签接口实现
 * <p>
 * <p/>
 * 功能更新历史：
 * <p>
 * ==================================================
 */
@Controller
@RequestMapping("api/")
public class AndroidNController {


    @Autowired
    private NewsService newsService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserAppService userAppService;

    //ID
    private final int idType=2;
    //阅读
    private final int rnumType=3;
    //点赞
    private final int zanType=4;
    //评论
    private final int commentType=5;

    /**
     * 分类，来源，阅读量，主页，分页查询接口
     * @param pageVo 分页对象
     * 必须的参数：
     *               1. p : 当前页；
     *               2. num: 请求总数；
     *               3. type : 操作类型：阅读量排序3，点赞排序4，评论排序5，正常请求2
     *               4. nType : 查询方式 ： 正常方式6,分类查询7，来源查询8
     * @return json : 判断有没有数据 根据data.size
     */
    @RequestMapping("getNesList")
    public @ResponseBody String getNesList(@ModelAttribute PageVo pageVo){

        if(pageVo.getP()<0){
            pageVo.setP(1);
        }
        pageVo.setStart((pageVo.getP()-1)*pageVo.getNum());//开始页面

        DataBean<List<NewsCustom>> bean = new DataBean<List<NewsCustom>>();
        try{
            List<NewsCustom> news = getNews(pageVo);
            if(news==null){
                news=new ArrayList<NewsCustom>();
            }
            bean.setCode(0);
            bean.setMsg("成功");
            bean.setData(news);
        } catch (Exception e) {
            bean.setCode(-3);
            bean.setMsg("系统错误");
        }
        return JSON.toJSONString(bean);
    }

    /**
     * 获取推荐新闻列表-分页实现
     * @param p 分页
     * @param num 数量
     * @param uid 用户id
     * @param type 类型
     * @return
     */
    @RequestMapping("getNesTuiList")
    public @ResponseBody String getNesTuiList(@RequestParam Integer p,@RequestParam Integer num,@RequestParam Integer uid,@RequestParam Integer type){
        DataBean<List<NewsCustom>> bean = new DataBean<List<NewsCustom>>();
        if(uid>0){
            if(p<0){
                p=1;
            }
            if(num<0){
                num=10;
            }
            int startIndex=(p-1)*num;
            int endIndex=startIndex+num;

            try {
                List<Integer> newsIds = FileTool.readData(uid);
                if(newsIds!=null){

                    if(endIndex>newsIds.size()){
                        endIndex=startIndex+(endIndex-newsIds.size());
                    }

                    if(startIndex<newsIds.size() && endIndex<=newsIds.size()) {
                        List<Integer> nids = newsIds.subList(startIndex, endIndex);
                        if(nids.size()==0){
                            bean.setCode(-1);
                            bean.setMsg("没有数据");
                        }else{
                            List<NewsCustom> nidsNews = newsService.getNidsNews(nids, type);
                            bean.setData(nidsNews);
                            bean.setCode(0);
                            bean.setMsg("成功");
                        }
                    }else{
                        bean.setCode(-1);
                        bean.setMsg("没有数据");
                    }

                }else{
                    bean.setCode(-1);
                    bean.setMsg("没有数据");
                }
            } catch (Exception e) {
                bean.setCode(-3);
                bean.setMsg("系统错误");
            }
        }else{
            bean.setCode(-2);
            bean.setMsg("uid 参数不正确");
        }
        return JSON.toJSONString(bean);
    }

    /**
     * 请求数据
     * @param pageVo 分页对象
     * @return list
     * @throws Exception
     */
    private List<NewsCustom> getNews(@ModelAttribute PageVo pageVo) throws Exception {
        //list数据
        switch (pageVo.getType()) {
            case idType:
                return newsService.getIdNews(pageVo);
            case rnumType:
                return newsService.getRnumNews(pageVo);
            case zanType:
                return newsService.getZanNews(pageVo);
            case commentType:
                return newsService.getCommentNews(pageVo);
            default:
                return newsService.getIdNews(pageVo);
        }
    }


    /**
     * 获取分类和来源信息
     * @return json
     */
    @RequestMapping("getCateSources")
    public @ResponseBody String getCateSources(){
        DataBean<CSCustom> bean = new DataBean<CSCustom>();
        try{
            CSCustom cateSourceIfo = newsService.findCateSourceIfo();
            bean.setCode(0);
            bean.setMsg("成功");
            bean.setData(cateSourceIfo);
        } catch (Exception e) {
            bean.setCode(-3);
            bean.setMsg("系统错误");
        }
        return JSON.toJSONString(bean);
    }


    /**
     * 新闻详情页接口
     * @param nid 新闻id
     * @return json
     */
    @RequestMapping("getNewsDetail")
    public @ResponseBody String getNewsDetail(@RequestParam Integer nid){
        DataBean<NewsCustom> bean = new DataBean<NewsCustom>();
        try{

            NewsCustom newsCustom = userService.selectNewsDetailById(nid);
            bean.setCode(0);
            bean.setMsg("成功");
            bean.setData(newsCustom);
            //阅读+1
            newsService.updateNewsRnum(nid);
        } catch (Exception e) {
            bean.setCode(-3);
            bean.setMsg("系统错误");
        }
        return JSON.toJSONString(bean);
    }


    /**
     * 查询点赞人的头像
     * @param nid 新闻id
     * @return json
     */
    @RequestMapping("getLikedHead")
    public @ResponseBody String getLikedHead(@RequestParam Integer nid){
        DataBean<List<LikedVo>> bean = new DataBean<List<LikedVo>>();
        try{
            List<LikedVo> heads = userAppService.getLikedUserIfo(nid);
            if(heads==null){
                heads= new ArrayList<LikedVo>();
            }
            bean.setCode(0);
            bean.setMsg("成功");
            bean.setData(heads);
        } catch (Exception e) {
            bean.setCode(-3);
            bean.setMsg("系统错误");
        }
        return JSON.toJSONString(bean);
    }


    /**
     * 查询新闻评论
     * @param p 当前页面
     * @param num 数量
     * @param nid 新闻id
     * @return json
     */
    @RequestMapping("getNewsComment")
    public @ResponseBody String getNewsComment(@RequestParam Integer p,@RequestParam Integer num,@RequestParam Integer nid){

        if(p<0){
            p=1;
        }
        p=(p-1)*num;
        DataBean<List<CommentJo>> bean = new DataBean<List<CommentJo>>();
        try{
            List<CommentJo> commentJos = userService.selectComments(p, num, nid);
            if(commentJos==null){
                commentJos=new ArrayList<CommentJo>();
            }
            bean.setCode(0);
            bean.setMsg("成功");
            bean.setData(commentJos);
        } catch (Exception e) {
            bean.setCode(-3);
            bean.setMsg("系统错误");
        }
        return JSON.toJSONString(bean);
    }


    /**
     * 分页查询兴趣标签（用户不存在的）
     * @param p   当前页面
     * @param num 总数
     * @param    uid 不存在则传0
     * @return json
     */
    @RequestMapping("getNewsTaste")
    public @ResponseBody String getNewsTaste(@RequestParam Integer p,@RequestParam Integer num,@RequestParam Integer uid){
        if(p<0){
            p=1;
        }
        p=(p-1)*num;
        DataBean<Set<String>> bean = new DataBean<Set<String>>();
        try{
            Set<String> taste = userService.selectTaste(p, num, uid);
            if(taste==null){
                taste=new HashSet<String>();
            }
            bean.setCode(0);
            bean.setMsg("成功");
            bean.setData(taste);
        } catch (Exception e) {
            bean.setCode(-3);
            bean.setMsg("系统错误");
        }
        return JSON.toJSONString(bean);
    }

    /**
     * 获取当前用户，当前新闻的点赞状态
     * @param uid
     * @param nid
     * @return
     */
    @RequestMapping("getLikedApiStatus")
    public @ResponseBody String getLikedApiStatus(@RequestParam Integer uid,@RequestParam Integer nid){
        DataBean<Integer> bean = new DataBean<Integer>();
        try{
            Integer status = userService.selectZanStatus(uid, nid);
            bean.setCode(0);
            bean.setMsg("成功");
            bean.setData(status);
        } catch (Exception e) {
            bean.setCode(-3);
            bean.setMsg("系统错误");
        }
        return JSON.toJSONString(bean);
    }


    /**
     * 得到推荐新闻的通知内容
     * @param uid
     * @return
     */
    @RequestMapping("getTuijianNotification")
    public @ResponseBody String getTuijianNotification(@RequestParam Integer uid){
        DataBean<TuijianModel> bean = new DataBean<TuijianModel>();
            if(uid>0){
                try {
                    List<Integer> newsIds = FileTool.readData(uid);
                    if(newsIds!=null){
                        bean.setCode(0);
                        TuijianModel model = new TuijianModel();
                        if(newsIds.size()>0) {

                            model.setCount(newsIds.size());
                            NewsCustom newsCustom = userService.selectNewsDetailById(newsIds.get(0));
                            model.setNewsCustom(newsCustom);

                            bean.setMsg("成功");
                            bean.setData(model);

                        }else {
                            bean.setMsg("没有推荐内容");
                            model.setCount(0);
                            bean.setData(model);
                        }

                    }else{
                        bean.setCode(-1);
                        bean.setMsg("没有数据");
                    }
                } catch (Exception e) {
                    bean.setCode(-3);
                    bean.setMsg("系统错误");
                }
            }else{
                bean.setCode(-2);
                bean.setMsg("uid 参数不正确");
            }
        return JSON.toJSONString(bean);
    }















































}
