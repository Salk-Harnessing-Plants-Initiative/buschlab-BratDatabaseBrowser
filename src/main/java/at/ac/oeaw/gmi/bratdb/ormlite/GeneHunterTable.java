package at.ac.oeaw.gmi.bratdb.ormlite;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import javax.persistence.Column;

/**
 * Created by alexander.bindeus on 07.03.17.
 */
@DatabaseTable(tableName = "geneHunter")
public class GeneHunterTable {

    public static final String SOURCE_EXP_NAME = "Source_Experiment";

    GeneHunterTable() {}

    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
    private int id;

    @DatabaseField(columnName = SOURCE_EXP_NAME,foreign = true)
    private ExpTable sourceExpId;

    @Column(name = "Hunter_file")
    public String Hunter_file;

    @Column(name = "Original_file")
    public String Original_file;

    @Column(name = "Chromosome")
    public String Chromosome;

    @Column(name = "SNP_pos")
    public Integer SNP_pos;

    @Column(name = "GWAS_pvalue")
    public Double GWAS_pvalue;

    @Column(name = "MAC")
    public Integer MAC;

    @Column(name = "Threshold")
    public Double Threshold = Double.NaN;

    @Column(name = "FDR_rejected")
    // 300317: sqlite has not boolean type => use to int
    public Integer FDR_rejected;

    @Column(name = "FDR_padj")
    public Double FDR_padj = Double.NaN;

    @Column(name = "Bonferroni_threshold")
    public Double Bonferroni_threshold = Double.NaN;

    @Column(name = "BH_threshold")
    public Double BH_threshold = Double.NaN;

    //@Column(name = "BHY_threshold")
    //public Double BHY_threshold;

    @Column(name = "Gene_start")
    public Integer Gene_start;

    @Column(name = "Gene_end")
    public Integer Gene_end;

    @Column(name = "Gene_orientation")
    public String Gene_orientation;

    @Column(name = "Relative_Distance")
    public Integer Relative_Distance;

    @Column(name = "SNP_relative_position")
    public String SNP_relative_position;

    @Column(name = "target_AGI")
    public String target_AGI;

    @Column(name = "target_element_type")
    public String target_element_type;

    @Column(name = "target_sequence_type")
    public String target_sequence_type;

    @Column(name = "target_annotation")
    public String target_annotation;

    @Column(name = "target_attributes")
    public String target_attributes;

    public void setSourceExpId(ExpTable exp) {
        this.sourceExpId = exp;
    }

    public String getHunterFile() {
        return this.Hunter_file;
    }

    public int getSourceExpId() {
        return this.id;
    }

    //public ExpTable getSourceExp() {
    //    return this.sourceExpId;
    //}

    void setFdrThres(double thres) {
        this.Threshold = thres;
    }

    public double getFdrThres() {
        return Threshold;
    }
}
