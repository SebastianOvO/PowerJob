package com.github.kfcfans.oms.server.web.controller;

import com.github.kfcfans.oms.common.response.ResultDTO;
import com.github.kfcfans.oms.server.common.utils.ContainerTemplateGenerator;
import com.github.kfcfans.oms.server.common.utils.OmsFileUtils;
import com.github.kfcfans.oms.server.persistence.core.model.ContainerInfoDO;
import com.github.kfcfans.oms.server.persistence.core.repository.ContainerInfoRepository;
import com.github.kfcfans.oms.server.service.ContainerService;
import com.github.kfcfans.oms.server.web.request.GenerateContainerTemplateRequest;
import com.github.kfcfans.oms.server.web.request.SaveContainerInfoRequest;
import com.github.kfcfans.oms.server.web.response.ContainerInfoVO;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 容器信息控制层
 *
 * @author tjq
 * @since 2020/5/15
 */
@Slf4j
@RestController
@RequestMapping("/container")
public class ContainerController {

    @Resource
    private ContainerInfoRepository containerInfoRepository;

    @Resource
    private ContainerService containerService;

    @GetMapping("/downloadJar")
    public void downloadJar(String version, HttpServletResponse response) throws IOException {
        File file = containerService.fetchContainerJarFile(version);
        if (file.exists()) {
            OmsFileUtils.file2HttpResponse(file, response);
        }
    }

    @PostMapping("/downloadContainerTemplate")
    public void downloadContainerTemplate(@RequestBody GenerateContainerTemplateRequest req, HttpServletResponse response) throws IOException {
        File zipFile = ContainerTemplateGenerator.generate(req.getGroup(), req.getArtifact(), req.getName(), req.getPackageName(), req.getJavaVersion());
        OmsFileUtils.file2HttpResponse(zipFile, response);
    }

    @PostMapping("/jarUpload")
    public ResultDTO<String> fileUpload(@RequestParam("file") MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            return ResultDTO.failed("empty file");
        }
        return ResultDTO.success(containerService.uploadContainerJarFile(file));
    }

    @PostMapping("/save")
    public ResultDTO<Void> saveContainer(@RequestBody SaveContainerInfoRequest request) {
        containerService.save(request);
        return ResultDTO.success(null);
    }

    @GetMapping("/list")
    public ResultDTO<List<ContainerInfoVO>> listContainers(Long appId) {
        List<ContainerInfoVO> res = containerInfoRepository.findByAppId(appId).stream().map(ContainerController::convert).collect(Collectors.toList());
        return ResultDTO.success(res);
    }

    @GetMapping("/listDeployedWorker")
    public ResultDTO<List<String>> listDeployedWorker(Long appId, Long containerId) {
        // TODO：本地 ContainerManager 直接返回
        List<String> mock = Lists.newArrayList("192.168.1.1:9900", "192.168.1.1:9901");
        return ResultDTO.success(mock);
    }

    @GetMapping("/delete")
    public ResultDTO<Void> deleteContainer(Long appId, Long containerId) {
        // TODO: 先停止各个Worker的容器实例
        containerInfoRepository.deleteById(containerId);
        return ResultDTO.success(null);
    }


    private static ContainerInfoVO convert(ContainerInfoDO containerInfoDO) {
        ContainerInfoVO vo = new ContainerInfoVO();
        BeanUtils.copyProperties(containerInfoDO, vo);
        return vo;
    }
}