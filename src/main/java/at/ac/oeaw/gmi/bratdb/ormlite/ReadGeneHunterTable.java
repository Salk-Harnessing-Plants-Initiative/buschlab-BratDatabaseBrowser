package at.ac.oeaw.gmi.bratdb.ormlite;

import at.ac.oeaw.gmi.bratdb.app.ClsSetterString;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class ReadGeneHunterTable {

    private Double fdr = Double.NaN;
    private String firstline, hunterFile;
    private String seperator = "\t";
    private BufferedReader br;
    private final ArrayList<String> rowValues = new ArrayList<>();
    private final ArrayList<GeneHunterTable> hunterTab = new ArrayList<>();
    private final ArrayList<String> header = new ArrayList<>();

    public ReadGeneHunterTable(String Fname) throws FileNotFoundException {
        br = new BufferedReader(new FileReader(Fname));
        hunterFile = Fname.substring(Fname.lastIndexOf('/')+1,Fname.length());
    } //def. constructor

    public void getfirstline() {

        String strAux;
        try {
            firstline = this.br.readLine();
            header.add("Hunter_file");
            Collections.addAll(header, firstline.split(seperator));

            // in case of csv formated input
            if (header.size() < 3) { // first entry is manually added "Hunter_file", second is whole line
                header.remove(1);
                seperator = ",";
                Collections.addAll(header, firstline.split(seperator));
            }

            //for (String entry : header) {
            for (int i = 0; i<header.size();++i) {
                strAux = header.get(i);

                // checking FDR values
                if (strAux.contains("FDR")) {
                    if (strAux.contains("rejected")) {
                        String aux[] = header.get(i).split("_");
                        try {
                            fdr = Double.parseDouble(aux[1]);
                        } catch (NumberFormatException e) {
                            fdr = 0.05; // workaround for older hunterfiles - not showing threshold
                        }
                        System.out.println("fdr: " + fdr);
                        header.set(i,"FDR_rejected");
                        continue;
                    } else {
                        if (strAux.contains("adjusted")) {
                            header.set(i,"FDR_padj");
                            continue;
                        } else {
                            System.out.println("input error with " + strAux);
                        }
                    }
                }

                // checking threshold labeled columns
                if (strAux.contains("threshold")) {
                    if (strAux.contains("Bonferroni")) {
                        header.set(i,"Bonferroni_threshold");
                        continue;
                    }
                    if (strAux.contains("BH_")) {
                        header.set(i,"BH_threshold");
                        continue;
                    }
//                    if (strAux.contains("BHY_")) {
//                        header.set(i,"BHY_threshold");
//                        continue;
//                    }
                }

                /// translating old captions into new ones
                if (strAux.contentEquals("Distance Gene")) {
                    header.set(i,"Relative_Distance");
                    continue;
                }
                if (strAux.contentEquals("SNP relative position")) {
                    header.set(i,"SNP_relative_position");
                    continue;
                }
                if (strAux.contentEquals("long description")) {
                    header.set(i,"target_attributes");
                    continue;
                }
                // for legacy header GWAS_p-value
                if (strAux.contains("-")) {
                    strAux = strAux.replace("-","");
                    header.set(i,strAux);
                }
                if (strAux.contains(" ")) {
                    strAux = strAux.replace(" ","_");
                    header.set(i,strAux);
                } // target_AGI, GWAS_p-value
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readIn(ExpTable exp, DB dataBase) {
        ClsSetterString<GeneHunterTable> setFieldTable = new ClsSetterString<>();
        try {
            while ((firstline = br.readLine()) != null) {

                // workaround for misformated files
                if (firstline.startsWith("Original_"))
                    continue;

                rowValues.clear();
                rowValues.add(hunterFile);
                Collections.addAll(rowValues, firstline.split(seperator));

//                if(rowValues.size() < 3) {
//                    rowValues.remove(1);
//                    Collections.addAll(rowValues, firstline.split(","));
//                    Main.logger.info("switched column seperator for reading hunter file " +
//                            "to comma at line " + linecount);
//                }

                GeneHunterTable geneHunt = new GeneHunterTable();
                setFieldTable.setFieldsByAnnotationType(geneHunt,rowValues,header);
                geneHunt.setFdrThres(fdr);
                geneHunt.setSourceExpId(exp);

                hunterTab.add(geneHunt);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        dataBase.AddGeneHunterTablesAll(hunterTab);
    }
}
