package com.lolmeida.service.k8s;

import com.lolmeida.entity.core.Environment;
import com.lolmeida.entity.core.Stack;
import com.lolmeida.service.K8sService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * Service for handling different deployment strategies based on environment
 */
@ApplicationScoped
public class DeploymentService {

    @Inject
    K8sService k8sService;

    @ConfigProperty(name = "app.deployment.local.helm-path", defaultValue = "helm")
    String helmPath;

    @ConfigProperty(name = "app.deployment.prod.context", defaultValue = "prod-cluster")
    String prodKubeContext;

    @ConfigProperty(name = "app.deployment.staging.context", defaultValue = "docker-desktop")
    String stagingKubeContext;

    /**
     * Deploy stack based on environment strategy
     */
    public DeploymentResult deployStack(Environment env, Stack stack) {
        try {
            switch (env.name.toLowerCase()) {
                case "dev":
                    return handleDevDeploy(env, stack);
                case "staging":
                    return handleStagingDeploy(env, stack);
                case "prod":
                    return handleProdDeploy(env, stack);
                default:
                    return new DeploymentResult(false, "Unknown environment: " + env.name, null);
            }
        } catch (Exception e) {
            return new DeploymentResult(false, "Deployment failed: " + e.getMessage(), null);
        }
    }

    /**
     * DEV: Mock deployment (current behavior)
     */
    private DeploymentResult handleDevDeploy(Environment env, Stack stack) {
        // Simulate processing time
        try {
            Thread.sleep(800);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return new DeploymentResult(
            true,
            String.format("Stack %s simulated successfully in %s environment", stack.name, env.name),
            "MOCK_DEPLOYMENT"
        );
    }

    /**
     * STAGING: Deploy to local Kubernetes (docker-desktop)
     */
    private DeploymentResult handleStagingDeploy(Environment env, Stack stack) {
        try {
            // Generate values.yaml for the stack
            Response valuesResponse = k8sService.generateStackValues(env.id, stack.name);
            if (valuesResponse.getStatus() != 200) {
                return new DeploymentResult(false, "Failed to generate values.yaml", null);
            }
            String valuesYaml = (String) valuesResponse.getEntity();
            
            // Save values to temporary file
            Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
            Path valuesFile = tempDir.resolve(String.format("values-%s-%s.yaml", env.name, stack.name));
            Files.write(valuesFile, valuesYaml.getBytes());

            // Execute helm command for local deployment
            ProcessBuilder pb = new ProcessBuilder(
                helmPath, "upgrade", "--install",
                String.format("%s-%s", env.name, stack.name),
                "./charts/" + stack.name,  // Assuming charts are in charts/ directory
                "--values", valuesFile.toString(),
                "--kube-context", stagingKubeContext,
                "--timeout", "5m0s",
                "--wait"
            );

            Process process = pb.start();
            boolean finished = process.waitFor(5, TimeUnit.MINUTES);
            
            if (!finished) {
                process.destroyForcibly();
                return new DeploymentResult(false, "Deployment timeout after 5 minutes", null);
            }

            int exitCode = process.exitValue();
            
            // Clean up temporary file
            Files.deleteIfExists(valuesFile);

            if (exitCode == 0) {
                return new DeploymentResult(
                    true,
                    String.format("Stack %s deployed successfully to local Kubernetes (%s)", 
                                stack.name, stagingKubeContext),
                    String.format("HELM_LOCAL:%s-%s", env.name, stack.name)
                );
            } else {
                return new DeploymentResult(
                    false,
                    String.format("Helm deployment failed with exit code %d", exitCode),
                    null
                );
            }

        } catch (IOException | InterruptedException e) {
            return new DeploymentResult(
                false,
                "Local deployment failed: " + e.getMessage(),
                null
            );
        }
    }

    /**
     * PROD: Deploy to production Kubernetes cluster
     */
    private DeploymentResult handleProdDeploy(Environment env, Stack stack) {
        try {
            // Generate values.yaml for the stack
            Response valuesResponse = k8sService.generateStackValues(env.id, stack.name);
            if (valuesResponse.getStatus() != 200) {
                return new DeploymentResult(false, "Failed to generate values.yaml", null);
            }
            String valuesYaml = (String) valuesResponse.getEntity();
            
            // Save values to temporary file
            Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
            Path valuesFile = tempDir.resolve(String.format("values-%s-%s.yaml", env.name, stack.name));
            Files.write(valuesFile, valuesYaml.getBytes());

            // Execute helm command for production deployment
            ProcessBuilder pb = new ProcessBuilder(
                helmPath, "upgrade", "--install",
                String.format("%s-%s", env.name, stack.name),
                "./charts/" + stack.name,
                "--values", valuesFile.toString(),
                "--kube-context", prodKubeContext,
                "--namespace", "production",
                "--create-namespace",
                "--timeout", "10m0s",
                "--wait",
                "--atomic"  // Rollback on failure
            );

            Process process = pb.start();
            boolean finished = process.waitFor(10, TimeUnit.MINUTES);
            
            if (!finished) {
                process.destroyForcibly();
                return new DeploymentResult(false, "Production deployment timeout after 10 minutes", null);
            }

            int exitCode = process.exitValue();
            
            // Clean up temporary file
            Files.deleteIfExists(valuesFile);

            if (exitCode == 0) {
                return new DeploymentResult(
                    true,
                    String.format("Stack %s deployed successfully to production cluster", stack.name),
                    String.format("HELM_PROD:%s-%s", env.name, stack.name)
                );
            } else {
                return new DeploymentResult(
                    false,
                    String.format("Production helm deployment failed with exit code %d", exitCode),
                    null
                );
            }

        } catch (IOException | InterruptedException e) {
            return new DeploymentResult(
                false,
                "Production deployment failed: " + e.getMessage(),
                null
            );
        }
    }

    /**
     * Result object for deployment operations
     */
    public static class DeploymentResult {
        public final boolean success;
        public final String message;
        public final String deploymentId;

        public DeploymentResult(boolean success, String message, String deploymentId) {
            this.success = success;
            this.message = message;
            this.deploymentId = deploymentId;
        }

        public String toJson() {
            return String.format(
                "{\"status\": \"%s\", \"message\": \"%s\", \"deploymentId\": \"%s\", \"deployedAt\": \"%s\"}",
                success ? "SUCCESS" : "ERROR",
                message,
                deploymentId != null ? deploymentId : "null",
                java.time.Instant.now().toString()
            );
        }
    }
} 