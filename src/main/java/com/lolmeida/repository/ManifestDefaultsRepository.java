package com.lolmeida.repository;

import com.lolmeida.entity.k8s.AuthDefault;
import com.lolmeida.entity.k8s.ManifestDefault;
import com.lolmeida.entity.k8s.ServiceCategory;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

/**
 * Repository for accessing manifest defaults and related data from database
 */
@ApplicationScoped
public class ManifestDefaultsRepository implements PanacheRepository<ManifestDefault> {

    @PersistenceContext
    EntityManager em;

    // ========== SERVICE CATEGORIES ==========

    /**
     * Find all active service categories
     */
    public List<ServiceCategory> findAllActiveCategories() {
        TypedQuery<ServiceCategory> query = em.createQuery(
            "SELECT sc FROM ServiceCategory sc WHERE sc.isActive = true ORDER BY sc.name", 
            ServiceCategory.class
        );
        return query.getResultList();
    }

    /**
     * Find service category by name
     */
    public Optional<ServiceCategory> findCategoryByName(String categoryName) {
        TypedQuery<ServiceCategory> query = em.createQuery(
            "SELECT sc FROM ServiceCategory sc WHERE sc.name = :name AND sc.isActive = true", 
            ServiceCategory.class
        );
        query.setParameter("name", categoryName);
        try {
            return Optional.of(query.getSingleResult());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Get category by name or default fallback
     */
    public ServiceCategory getCategoryOrDefault(String categoryName) {
        return findCategoryByName(categoryName)
                .orElseGet(() -> findCategoryByName("default")
                        .orElse(null));
    }

    // ========== MANIFEST DEFAULTS ==========

    /**
     * Find all active manifest defaults for a category
     */
    public List<ManifestDefault> findManifestDefaultsByCategory(String categoryName) {
        return find("category.name = ?1 AND isActive = true ORDER BY creationPriority ASC", categoryName).list();
    }

    /**
     * Find manifest defaults by category ID
     */
    public List<ManifestDefault> findManifestDefaultsByCategoryId(Long categoryId) {
        return find("category.id = ?1 AND isActive = true ORDER BY creationPriority ASC", categoryId).list();
    }

    /**
     * Find specific manifest default by category and manifest type
     */
    public Optional<ManifestDefault> findManifestDefault(String categoryName, String manifestType) {
        return find("category.name = ?1 AND manifestType = ?2 AND isActive = true", 
                   categoryName, manifestType).firstResultOptional();
    }

    /**
     * Get all unique manifest types used across all categories
     */
    public List<String> getAllManifestTypes() {
        return getEntityManager()
                .createQuery("SELECT DISTINCT md.manifestType FROM ManifestDefault md WHERE md.isActive = true", String.class)
                .getResultList();
    }

    /**
     * Check if a manifest should be created based on conditions
     */
    public List<ManifestDefault> findRequiredManifests(String categoryName) {
        return find("category.name = ?1 AND required = true AND isActive = true ORDER BY creationPriority ASC", 
                   categoryName).list();
    }

    /**
     * Find conditional manifests (those with creation conditions)
     */
    public List<ManifestDefault> findConditionalManifests(String categoryName) {
        return find("category.name = ?1 AND creationCondition IS NOT NULL AND isActive = true ORDER BY creationPriority ASC", 
                   categoryName).list();
    }

    // ========== AUTH DEFAULTS ==========

    /**
     * Find all auth defaults for a category
     */
    public List<AuthDefault> findAuthDefaultsByCategory(String categoryName) {
        return AuthDefault.<AuthDefault>find("category.name = ?1 AND isActive = true ORDER BY authType ASC", categoryName).list();
    }

    /**
     * Find specific auth default by category and auth type
     */
    public Optional<AuthDefault> findAuthDefault(String categoryName, String authType) {
        return AuthDefault.<AuthDefault>find("category.name = ?1 AND authType = ?2 AND isActive = true", 
                                           categoryName, authType).firstResultOptional();
    }

    /**
     * Get all unique auth types used across all categories
     */
    public List<String> getAllAuthTypes() {
        return getEntityManager()
                .createQuery("SELECT DISTINCT ad.authType FROM AuthDefault ad WHERE ad.isActive = true", String.class)
                .getResultList();
    }

    /**
     * Find supported auth types for a specific category
     */
    public List<String> getAuthTypesForCategory(String categoryName) {
        return getEntityManager()
                .createQuery("SELECT ad.authType FROM AuthDefault ad WHERE ad.category.name = ?1 AND ad.isActive = true ORDER BY ad.authType ASC", String.class)
                .setParameter(1, categoryName)
                .getResultList();
    }

    // ========== UTILITY METHODS ==========

    /**
     * Count total active categories
     */
    public long countActiveCategories() {
        TypedQuery<Long> query = em.createQuery(
            "SELECT COUNT(sc) FROM ServiceCategory sc WHERE sc.isActive = true", 
            Long.class
        );
        return query.getSingleResult();
    }

    /**
     * Count manifest defaults by category
     */
    public long countManifestDefaultsByCategory(String categoryName) {
        return ManifestDefault.count("category.name = ?1 AND isActive = true", categoryName);
    }

    /**
     * Count auth defaults by category
     */
    public long countAuthDefaultsByCategory(String categoryName) {
        TypedQuery<Long> query = em.createQuery(
            "SELECT COUNT(ad) FROM AuthDefault ad WHERE ad.category.name = :categoryName AND ad.isActive = true", 
            Long.class
        );
        query.setParameter("categoryName", categoryName);
        return query.getSingleResult();
    }

    /**
     * Check if category exists and is active
     */
    public boolean categoryExists(String categoryName) {
        TypedQuery<Long> query = em.createQuery(
            "SELECT COUNT(sc) FROM ServiceCategory sc WHERE sc.name = :name AND sc.isActive = true", 
            Long.class
        );
        query.setParameter("name", categoryName);
        return query.getSingleResult() > 0;
    }

    /**
     * Get complete category information with counts
     */
    public List<Object[]> getCategoryStats() {
        return getEntityManager()
                .createQuery("""
                    SELECT c.name, c.displayName, c.description, c.icon, c.color,
                           COUNT(DISTINCT md.id) as manifestCount,
                           COUNT(DISTINCT ad.id) as authCount
                    FROM ServiceCategory c
                    LEFT JOIN c.manifestDefaults md ON md.isActive = true
                    LEFT JOIN c.authDefaults ad ON ad.isActive = true
                    WHERE c.isActive = true
                    GROUP BY c.id, c.name, c.displayName, c.description, c.icon, c.color
                    ORDER BY c.name
                    """)
                .getResultList();
    }
} 