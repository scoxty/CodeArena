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
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component("CExecutionStrategy")
public class CExecutionStrategy implements CodeExecutionStrategy {
    private static final String IMAGE = "c-sandbox:latest";

    @Autowired
    private final DockerClient dockerClient;

    @Autowired
    private ThreadPoolExecutor sandboxExecutor;

    public CExecutionStrategy(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
    }

    @Override
    public Integer executeCode(Integer userId, String code, String input) {
        try {
            String containerId = createAndStartContainer();

            Integer result = executeCCodeInContainer(containerId, code, input);

            sandboxExecutor.submit(()->{
                dockerClient.removeContainerCmd(containerId).withForce(true).exec();
            });

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String createAndStartContainer() {
        HostConfig hostConfig = new HostConfig();
        hostConfig.withMemory(200 * 1000 *1000L); // 限制内存200MB
        hostConfig.withMemorySwap(0L); // 禁止使用交换空间，防止容器过度使用硬盘空间作为虚拟内存
        hostConfig.withCpuCount(1L); // 限制cpu为1核

        CreateContainerResponse container = dockerClient.createContainerCmd(IMAGE)
                .withHostConfig(hostConfig)
                .withAttachStdin(true)
                .withAttachStderr(true)
                .withAttachStdout(true)
                .withNetworkDisabled(true) // 禁用网络
                .withTty(true)
                .withCmd("tail", "-f", "/dev/null") // 保持容器运行
                .exec();

        dockerClient.startContainerCmd(container.getId()).exec();

        return container.getId();
    }

    private Integer executeCCodeInContainer(String containerId, String code, String input) {
        String codeBase64 = Base64.getEncoder().encodeToString(code.getBytes());
        String inputBase64 =  Base64.getEncoder().encodeToString(input.getBytes());

        String command = String.format("/bin/sh -c \"echo '%s' | base64 -d > /sandbox/Code.c && " +
                "echo '%s' | base64 -d > /sandbox/input.txt && " +
                "gcc /sandbox/Code.c -o /sandbox/Code && " +
                "/sandbox/Code < /sandbox/input.txt\"", codeBase64, inputBase64);

        ExecCreateCmdResponse execResponse = dockerClient.execCreateCmd(containerId)
                .withCmd("sh", "-c", command)
                .withAttachStdin(false)
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
