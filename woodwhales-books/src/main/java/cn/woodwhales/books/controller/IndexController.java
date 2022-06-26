package cn.woodwhales.books.controller;

import cn.hutool.core.io.FileUtil;
import cn.woodwhales.books.model.MdParam;
import cn.woodwhales.common.business.DataTool;
import cn.woodwhales.common.model.vo.RespVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author woodwhales on 2022-06-26 17:45
 */
@Slf4j
@RequestMapping("/")
@RestController
public class IndexController {

    private static Set<String> ignoreFileNameSet = new HashSet<>();

    static {
        ignoreFileNameSet.add(".git");
        ignoreFileNameSet.add("woodwhales-books");
        ignoreFileNameSet.add(".gitignore");
        ignoreFileNameSet.add("LICENSE");
        ignoreFileNameSet.add("README.md");
        ignoreFileNameSet.add(".idea");
        ignoreFileNameSet.add(".gitattributes");
    }

    @PostMapping("md")
    public RespVO<String> md(@RequestBody MdParam param) {
        String path = param.getPath();
        File file = new File(path);
        if (!file.exists()) {
            return RespVO.errorWithErrorMsg("文件路径不存在");
        }
        if (!file.isDirectory()) {
            return RespVO.errorWithErrorMsg("文件目录路径不合法");
        }
        File[] files = file.listFiles();
        if (Objects.isNull(files)) {
            return RespVO.errorWithErrorMsg("该文件目录下无文件");
        }

        List<File> fileList = DataTool.filter(files, myFile -> !ignoreFileNameSet.contains(myFile.getName()));
        String rootPath = "";
        StringBuffer sb = new StringBuffer();
        sb.append("# woodwhales-books\n" +
                "> 图书丈量世界").append("\n");
        for (File subFile : fileList) {
            AtomicInteger level = new AtomicInteger(1);
            list(sb, level, subFile, rootPath + "/" + subFile.getName());
            sb.append("\n");
        }
        log.info("{}", sb.toString());
        return RespVO.success(sb.toString());
    }

    private void list(StringBuffer sb, AtomicInteger level, File subFile, String rootPath) {
        if (subFile.isDirectory()) {
            level.incrementAndGet();
            sb.append(this.title(level) + subFile.getName());
            sb.append("\n");

            File[] files = subFile.listFiles();
            if (Objects.nonNull(files)) {
                DataTool.filter(files, childrenFile -> !childrenFile.isDirectory())
                        .forEach(childrenFile -> {
                                    sb.append(fileName(childrenFile, rootPath + "/"));
                                    sb.append("\n");
                                    sb.append("\n");
                                }
                        );

                sb.append("\n");

                List<File> childrenFileList = DataTool.filter(files, File::isDirectory);
                for (File childrenFile : childrenFileList) {
                    level.incrementAndGet();
                    list(sb, new AtomicInteger(level.get()), childrenFile, rootPath + "/" + childrenFile.getName());
                    level.decrementAndGet();
                }
            }
        } else {
            sb.append(fileName(subFile, rootPath + "/"));
            sb.append("\n");
            sb.append("\n");
        }
    }

    private String title(AtomicInteger level) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level.get(); i++) {
            sb.append("#");
        }
        sb.append(" ");
        return sb.toString();
    }

    private String fileName(File file, String path) {
        return String.format("[%s](%s)", file.getName(), path + file.getName());
    }

}
