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

@Component("JavaExecutionStrategy")
public class JavaExecutionStrategy implements CodeExecutionStrategy {
    private static final String IMAGE = "openjdk:11-jdk"; // 提前下载好

    @Autowired
    private final DockerClient dockerClient;

    public JavaExecutionStrategy(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
    }

    @Override
    public Integer executeCode(String code, String input) {
        try {
            Path codePath = Paths.get("/home/xty/codearena/backendcloud/botrunning/java/Code.java");
            Path inputPath = Paths.get("/home/xty/codearena/backendcloud/botrunning/input.txt");
            Files.writeString(codePath, code);
            Files.writeString(inputPath, input);

            String containerId = createAndStartContainer();

            Integer result = executeJavaCodeInContainer(containerId);

            dockerClient.removeContainerCmd(containerId).withForce(true).exec();

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String createAndStartContainer() {
        HostConfig hostConfig = new HostConfig();
        Bind bind = new Bind("/home/xty/codearena/backendcloud/botrunning",new Volume("/botrunning"));
        hostConfig.setBinds(bind);
        hostConfig.withMemory(200 * 1000 *1000L); // 限制内存200MB
        hostConfig.withMemorySwap(0L); // 禁止使用交换空间，防止容器过度使用硬盘空间作为虚拟内存
        hostConfig.withCpuCount(1L); // 限制cpu为1核

        CreateContainerResponse container = dockerClient.createContainerCmd(IMAGE)
                .withHostConfig(hostConfig)
                .withAttachStdin(true)
                .withAttachStderr(true)
                .withAttachStdout(true)
                .withNetworkDisabled(true)
                .withTty(true)
                .withCmd("tail", "-f", "/dev/null") // 保持容器运行
                .exec();

        dockerClient.startContainerCmd(container.getId()).exec();

        return container.getId();
    }

    private Integer executeJavaCodeInContainer(String containerId) {
        // 编译Java代码
        ExecCreateCmdResponse compileResponse = dockerClient.execCreateCmd(containerId)
                .withCmd("javac", "/botrunning/java/Code.java")
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

        // 执行Java代码并捕获标准输出
        ExecCreateCmdResponse execResponse = dockerClient.execCreateCmd(containerId)
                .withCmd("java", "-cp", "/botrunning/java", "Code")
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




