package cn.bincker.modules.blog.mapper;

import cn.bincker.modules.blog.entity.Blog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Optional;

@Mapper
public interface BlogMapper extends BaseMapper<Blog> {
    @Select("select * from blog where file_path = #{path}")
    Optional<Blog> selectByFilePathWithoutDeleted(@Param("path") String path);

    @Update("update blog set title = #{blog.title}, file_last_modified = #{blog.fileLastModified}, deleted = #{blog.deleted} where id = #{blog.id}")
    void updateBaseInfoWithoutDeleted(@Param("blog") Blog blog);
}
