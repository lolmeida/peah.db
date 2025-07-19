package com.lolmeida.peahdb.dto.mapper;

// K8s entities imports
import com.lolmeida.peahdb.dto.request.*;
import com.lolmeida.peahdb.dto.response.*;
import com.lolmeida.peahdb.entity.k8s.*;

import org.mapstruct.*;

@Mapper(
        componentModel = MappingConstants.ComponentModel.JAKARTA_CDI,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
)
public interface K8sMapperService {
    
    
    // ========== K8S ENTITY MAPPERS ==========
    
    // App mappers
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "requiredManifests", ignore = true)
    @Mapping(source = "stackId", target = "stack.id")
    App toApp(AppRequest appRequest);
    
    @Mapping(source = "appRequest.stackId", target = "stack.id")
    @Mapping(source = "id", target = "id")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "requiredManifests", ignore = true)
    App toAppWithId(AppRequest appRequest, Long id);
    
    // AppManifest mappers
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(source = "appId", target = "app.id")
    AppManifest toAppManifest(AppManifestRequest appManifestRequest);
    
    @Mapping(source = "appManifestRequest.appId", target = "app.id")
    @Mapping(source = "id", target = "id")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    AppManifest toAppManifestWithId(AppManifestRequest appManifestRequest, Long id);
    
    // Deployment mappers
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(source = "serviceId", target = "service.id")
    Deployment toDeployment(DeploymentRequest deploymentRequest);
    
    @Mapping(source = "deploymentRequest.serviceId", target = "service.id")
    @Mapping(source = "id", target = "id")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Deployment toDeploymentWithId(DeploymentRequest deploymentRequest, Long id);
    
    // Secret mappers
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(source = "serviceId", target = "service.id")
    Secret toSecret(SecretRequest secretRequest);
    
    @Mapping(source = "secretRequest.serviceId", target = "service.id")
    @Mapping(source = "id", target = "id")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Secret toSecretWithId(SecretRequest secretRequest, Long id);
    
    // ConfigMap mappers
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(source = "serviceId", target = "service.id")
    ConfigMap toConfigMap(ConfigMapRequest configMapRequest);
    
    @Mapping(source = "configMapRequest.serviceId", target = "service.id")
    @Mapping(source = "id", target = "id")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ConfigMap toConfigMapWithId(ConfigMapRequest configMapRequest, Long id);
    
    // K8sService mappers
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(source = "serviceId", target = "service.id")
    K8sService toK8sService(K8sServiceRequest k8sServiceRequest);
    
    @Mapping(source = "k8sServiceRequest.serviceId", target = "service.id")
    @Mapping(source = "id", target = "id")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    K8sService toK8sServiceWithId(K8sServiceRequest k8sServiceRequest, Long id);
    
    // Ingress mappers
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(source = "serviceId", target = "service.id")
    Ingress toIngress(IngressRequest ingressRequest);
    
    @Mapping(source = "ingressRequest.serviceId", target = "service.id")
    @Mapping(source = "id", target = "id")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Ingress toIngressWithId(IngressRequest ingressRequest, Long id);
    
    // PersistentVolumeClaim mappers
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(source = "serviceId", target = "service.id")
    PersistentVolumeClaim toPersistentVolumeClaim(PersistentVolumeClaimRequest persistentVolumeClaimRequest);
    
    @Mapping(source = "persistentVolumeClaimRequest.serviceId", target = "service.id")
    @Mapping(source = "id", target = "id")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    PersistentVolumeClaim toPersistentVolumeClaimWithId(PersistentVolumeClaimRequest persistentVolumeClaimRequest, Long id);
    
    // ServiceAccount mappers
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(source = "serviceId", target = "service.id")
    ServiceAccount toServiceAccount(ServiceAccountRequest serviceAccountRequest);
    
    @Mapping(source = "serviceAccountRequest.serviceId", target = "service.id")
    @Mapping(source = "id", target = "id")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ServiceAccount toServiceAccountWithId(ServiceAccountRequest serviceAccountRequest, Long id);
    
    // HPA mappers
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(source = "serviceId", target = "service.id")
    Hpa toHpa(HpaRequest hpaRequest);
    
    @Mapping(source = "hpaRequest.serviceId", target = "service.id")
    @Mapping(source = "id", target = "id")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Hpa toHpaWithId(HpaRequest hpaRequest, Long id);
    
    // ClusterRole mappers
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(source = "serviceId", target = "service.id")
    ClusterRole toClusterRole(ClusterRoleRequest clusterRoleRequest);
    
    @Mapping(source = "clusterRoleRequest.serviceId", target = "service.id")
    @Mapping(source = "id", target = "id")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ClusterRole toClusterRoleWithId(ClusterRoleRequest clusterRoleRequest, Long id);
    
    // ========== K8S ENTITY TO RESPONSE MAPPERS ==========
    
    // App response mappers
    @Mapping(source = "stack.id", target = "stackId")
    AppResponse toAppResponse(App app);
    
    // AppManifest response mappers
    @Mapping(source = "app.id", target = "appId")
    AppManifestResponse toAppManifestResponse(AppManifest appManifest);
    
    // Deployment response mappers
    @Mapping(source = "service.id", target = "serviceId")
    DeploymentResponse toDeploymentResponse(Deployment deployment);
    
    // Secret response mappers
    @Mapping(source = "service.id", target = "serviceId")
    SecretResponse toSecretResponse(Secret secret);
    
    // ConfigMap response mappers
    @Mapping(source = "service.id", target = "serviceId")
    ConfigMapResponse toConfigMapResponse(ConfigMap configMap);
    
    // K8sService response mappers
    @Mapping(source = "service.id", target = "serviceId")
    K8sServiceResponse toK8sServiceResponse(K8sService k8sService);
    
    // Ingress response mappers
    @Mapping(source = "service.id", target = "serviceId")
    IngressResponse toIngressResponse(Ingress ingress);
    
    // PersistentVolumeClaim response mappers
    @Mapping(source = "service.id", target = "serviceId")
    PersistentVolumeClaimResponse toPersistentVolumeClaimResponse(PersistentVolumeClaim persistentVolumeClaim);
    
    // ServiceAccount response mappers
    @Mapping(source = "service.id", target = "serviceId")
    ServiceAccountResponse toServiceAccountResponse(ServiceAccount serviceAccount);
    
    // HPA response mappers
    @Mapping(source = "service.id", target = "serviceId")
    HpaResponse toHpaResponse(Hpa hpa);
    
    // ClusterRole response mappers
    @Mapping(source = "service.id", target = "serviceId")
    ClusterRoleResponse toClusterRoleResponse(ClusterRole clusterRole);
}
