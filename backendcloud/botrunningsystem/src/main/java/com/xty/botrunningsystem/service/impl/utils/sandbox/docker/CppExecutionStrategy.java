package com.xty.botrunningsystem.service.impl.utils.sandbox.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Volume;
import com.xty.botrunningsystem.service.impl.utils.sandbox.CodeExecutionStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component("CppExecutionStrategy")
public class CppExecutionStrategy implements CodeExecutionStrategy {
    private static final String IMAGE = "gcc:latest";

    @Autowired
    private final DockerClient dockerClient;

    public CppExecutionStrategy(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
    }

    @Override
    public Integer executeCode(String code, String input) {
        try {
            Path codePath = Paths.get("/home/xty/codearena/backendcloud/botrunning/cpp/Code.cpp");
            Path inputPath = Paths.get("/home/xty/codearena/backendcloud/botrunning/input.txt");
            Files.writeString(codePath, code);
            Files.writeString(inputPath, input);

            String containerId = createAndStartContainer();

            Integer result = executeCCodeInContainer(containerId);

            dockerClient.removeContainerCmd(containerId).withForce(true).exec();

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String createAndStartContainer() {
        HostConfig hostConfig = new HostConfig();
        Bind bind = new Bind("/home/xty/codearena/backendcloud/botrunning", new Volume("/botrunning"));
        hostConfig.setBinds(bind);
        hostConfig.withMemory(200 * 1000 * 1000L);
        hostConfig.withMemorySwap(0L);
        hostConfig.withCpuCount(1L);

        CreateContainerResponse container = dockerClient.createContainerCmd(IMAGE)
                .withHostConfig(hostConfig)
                .withAttachStdin(true)
                .withAttachStderr(true)
                .withAttachStdout(true)
                .withNetworkDisabled(true)
                .withTty(true)
                .withCmd("tail", "-f", "/dev/null")
                .exec();

        dockerClient.startContainerCmd(container.getId()).exec();

        return container.getId();
    }

    private Integer executeCCodeInContainer(String containerId) {
        // 编译 C++ 代码
        ExecCreateCmdResponse compileResponse = dockerClient.execCreateCmd(containerId)
                .withCmd("g++", "/botrunning/cpp/Code.cpp", "-o", "/botrunning/cpp/Code")
                .withAttachStdin(true)
                .withAttachStderr(true)
                .withAttachStdout(true)
                .exec();
        try {
            MyExecStartResultCallback compileResultCallback = new MyExecStartResultCallback();
            dockerClient.execStartCmd(compileResponse.getId()).exec(compileResultCallback);
            compileResultCallback.awaitCompletion();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 执行编译后的 C++ 程序
        ExecCreateCmdResponse execResponse = dockerClient.execCreateCmd(containerId)
                .withCmd("/botrunning/cpp/Code")
                .withAttachStdin(true)
                .withAttachStderr(true)
                .withAttachStdout(true)
                .exec();

        Integer res = null;
        MyExecStartResultCallback execResultCallback = new MyExecStartResultCallback();
        try {
            dockerClient.execStartCmd(execResponse.getId()).exec(execResultCallback);
            String output = execResultCallback.awaitCompletion().trim();
            res = Integer.parseInt(output);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return res;
    }
}
