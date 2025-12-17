package cn.kmbeast.service.impl;

import cn.kmbeast.mapper.BookMapper;
import cn.kmbeast.pojo.api.ApiResult;
import cn.kmbeast.pojo.api.PageResult;
import cn.kmbeast.pojo.api.Result;
import cn.kmbeast.pojo.dto.query.extend.BookQueryDto;
import cn.kmbeast.pojo.entity.Book;
import cn.kmbeast.pojo.vo.BookVO;
import cn.kmbeast.service.BookService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 书籍业务逻辑实现
 */
@Service
public class BookServiceImpl implements BookService {

    @Resource
    private BookMapper bookMapper;

    /**
     * 书籍新增
     *
     * @param book 参数
     * @return Result<Void>
     */
    @Override
    public Result<Void> save(Book book) {
        if (book.getIsPlanBuy() == null) {
            book.setIsPlanBuy(false);
        }
        book.setCreateTime(LocalDateTime.now());
        bookMapper.save(book);
        return ApiResult.success();
    }

    /**
     * 书籍删除
     *
     * @param ids 参数
     * @return Result<Void>
     */
    @Override
    public Result<Void> batchDelete(List<Integer> ids) {
        bookMapper.batchDelete(ids);
        return ApiResult.success();
    }

    /**
     * 书籍修改
     *
     * @param book 参数
     * @return Result<Void>
     */
    @Override
    public Result<Void> update(Book book) {
        bookMapper.update(book);
        return ApiResult.success();
    }

    /**
     * 书籍查询
     *
     * @param bookQueryDto 查询参数
     * @return Result<List < BookVO>>
     */
    @Override
    public Result<List<BookVO>> query(BookQueryDto bookQueryDto) {
        List<BookVO> noticeList = bookMapper.query(bookQueryDto);
        Integer totalCount = bookMapper.queryCount(bookQueryDto);
        return PageResult.success(noticeList, totalCount);
    }


}
