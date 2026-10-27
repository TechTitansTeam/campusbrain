package com.campusbrain.repository;

import com.campusbrain.entity.AcademicMaterial;
import com.campusbrain.entity.MaterialCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AcademicMaterialRepository extends JpaRepository<AcademicMaterial, Long>, JpaSpecificationExecutor<AcademicMaterial> {

    List<AcademicMaterial> findAllByPublishedTrueOrderByCreatedAtDesc();

    List<AcademicMaterial> findAllByOrderByCreatedAtDesc();

    long countByCategory(MaterialCategory category);

    long countByCategoryAndPublishedTrue(MaterialCategory category);

    long countByPublishedTrue();

    long countByPublishedFalse();

    @Query("SELECT DISTINCT m.subject FROM AcademicMaterial m WHERE m.published = true ORDER BY m.subject ASC")
    List<String> findDistinctSubjectsByPublishedTrue();

    @Query("SELECT DISTINCT m.academicYear FROM AcademicMaterial m WHERE m.published = true ORDER BY m.academicYear ASC")
    List<String> findDistinctAcademicYearsByPublishedTrue();

    @Query("SELECT DISTINCT m.semester FROM AcademicMaterial m WHERE m.published = true ORDER BY m.semester ASC")
    List<String> findDistinctSemestersByPublishedTrue();

    @Query("SELECT DISTINCT m.fileExtension FROM AcademicMaterial m WHERE m.published = true ORDER BY m.fileExtension ASC")
    List<String> findDistinctFileExtensionsByPublishedTrue();
}
