package edu.cmu.lti.event_coref.util;

/**
 * Constants related to semantic roles.
 * 
 * @author Jun Araki
 */
public class SemanticRoleConstants {

  /**
   * Generic semantic roles.  See the following for more details:
   * http://en.wikipedia.org/wiki/Thematic_relation
   * 
   * @author Jun Araki
   */
  public enum SemanticRole {
    AGENT (PropBankSemanticRole.ARG0),
    PATIENT (PropBankSemanticRole.ARG1),
    LOCATION (PropBankSemanticRole.ARGM_LOC),
    TIME (PropBankSemanticRole.ARGM_TMP);

    private PropBankSemanticRole propBankSemRole;

    SemanticRole(PropBankSemanticRole propBankSemRole) {
      this.propBankSemRole = propBankSemRole;
    }

    public PropBankSemanticRole getPropBankSemRole() {
      return propBankSemRole;
    }

    public static SemanticRole findByPropBankSemanticRole(PropBankSemanticRole propBankSemRole) {
      for (SemanticRole semRole : SemanticRole.values()) {
        if (propBankSemRole.equals(semRole.getPropBankSemRole())) {
          return semRole;
        }
      }

      return null;
    }
  }

  /**
   * Resolve the following.
   * 
   * Fanse
   * [ARG0, ARG0-INVERTED, ARG1, ARG1-INVERTED, ARG1-REC, ARG2, ARG2-DIR, ARG2-EXT, ARG2-INVERTED, ARG2-MNR, ARG2-PNC, ARG3, ARG3-MNR, ARG4, ARG5-DIR, ARGM-ADV, ARGM-CAU, ARGM-DIR, ARGM-DIS, ARGM-EXT, ARGM-LOC, ARGM-MNR, ARGM-MOD, ARGM-NEG, ARGM-PNC, ARGM-TMP]
   * 
   * ClearNLP
   * [A0, A1, A1-DSP, A2, A3, A4, AM-ADV, AM-CAU, AM-COM, AM-DIR, AM-DIS, AM-EXT, AM-GOL, AM-LOC, AM-MNR, AM-MOD, AM-NEG, AM-PNC, AM-PRD, AM-PRP, AM-PRR, AM-TMP, C-A1, C-V, R-A0, R-A1, R-A2, R-AM-CAU, R-AM-LOC, R-AM-TMP]
   * 
   * http://clear.colorado.edu/compsem/documents/propbank_guidelines.pdf
   * 
   * @author Jun Araki
   */
  public enum PropBankSemanticRole {
    // TODO: implement more.
    ARG0, ARG1, ARG2, ARG3, ARG4,
    // Modifiers
    ARGM_COM,  // Comitatives
    ARGM_DIR,  // Directional
    ARGM_LOC,  // Locatives
    ARGM_GOL,  // Goal
    ARGM_MNR,  // Manner
    ARGM_TMP,  // Temporal
    ARGM_EXT,  // Extent
    ARGM_REC,  // Reciprocals
    ARGM_PRD,  // Secondary predication
    ARGM_PRP,  // Purpose clause
    ARGM_PNC,  // Purpose (not clause)
    ARGM_PRR,  // Predicating relation
    ARGM_CAU,  // cause
    ARGM_DIS,  // discourse
    ARGM_ADV,  // adverbials
    ARGM_MOD,  // modal
    ARGM_NEG;  // negation
  }

  /**
   * PropBank-style semantic roles used in FANSE. 
   * 
   * @author Jun Araki
   */
  public enum FanseSemanticRole {
    ARG0 (PropBankSemanticRole.ARG0, "ARG0"),
    ARG0_INVERTED (PropBankSemanticRole.ARG0, "ARG0-INVERTED"),
    ARG1 (PropBankSemanticRole.ARG1, "ARG1"),
    ARG1_INVERTED (PropBankSemanticRole.ARG1, "ARG1-INVERTED"),
    ARG2 (PropBankSemanticRole.ARG2, "ARG2"),
    ARG2_INVERTED (PropBankSemanticRole.ARG2, "ARG2-INVERTED"),
    ARG3 (PropBankSemanticRole.ARG3, "ARG3"),
    ARG3_INVERTED (PropBankSemanticRole.ARG3, "ARG3-INVERTED"),
    ARG4 (PropBankSemanticRole.ARG4, "ARG4"),
    ARG4_INVERTED (PropBankSemanticRole.ARG4, "ARG4-INVERTED"),
    ARGM_DIR (PropBankSemanticRole.ARGM_DIR, "ARGM-DIR"),
    ARGM_LOC (PropBankSemanticRole.ARGM_LOC, "ARGM-LOC"),
    ARGM_MNR (PropBankSemanticRole.ARGM_MNR, "ARGM-MNR"),
    ARGM_TMP (PropBankSemanticRole.ARGM_TMP, "ARGM-TMP"),
    ARGM_EXT (PropBankSemanticRole.ARGM_EXT, "ARGM-EXT"),
    ARGM_REC (PropBankSemanticRole.ARGM_REC, "ARGM-REC"),
    ARGM_PRD (PropBankSemanticRole.ARGM_PRD, "ARGM-PRD"),
    ARGM_PNC (PropBankSemanticRole.ARGM_PNC, "ARGM-PNC"),
    ARGM_CAU (PropBankSemanticRole.ARGM_CAU, "ARGM-CAU"),
    ARGM_DIS (PropBankSemanticRole.ARGM_DIS, "ARGM-DIS"),
    ARGM_ADV (PropBankSemanticRole.ARGM_ADV, "ARGM-ADV"),
    ARGM_MOD (PropBankSemanticRole.ARGM_MOD, "ARGM-MOD"),
    ARGM_NEG (PropBankSemanticRole.ARGM_NEG, "ARGM-NEG");

    private PropBankSemanticRole propBankSemRole;
    private String fanseSemRoleStr;

    FanseSemanticRole(PropBankSemanticRole propBankSemRole, String fanseSemRoleStr) {
      this.propBankSemRole = propBankSemRole;
      this.fanseSemRoleStr = fanseSemRoleStr;
    }

    public PropBankSemanticRole getPropBankSemRole() {
      return propBankSemRole;
    }

    public boolean isInverted() {
      return fanseSemRoleStr.endsWith("-INVERTED");
    }

    @Override
    public String toString() {
      return fanseSemRoleStr;
    }
  }


  /**
   * PropBank-style semantic roles used in ClearNLP. 
   * 
   * @author Jun Araki
   */
  public enum ClearnlpSemanticRole {
    ARG0 (PropBankSemanticRole.ARG0, "A0"),
    ARG1 (PropBankSemanticRole.ARG1, "A1"),
    ARG2 (PropBankSemanticRole.ARG2, "A2"),
    ARG3 (PropBankSemanticRole.ARG3, "A3"),
    ARG4 (PropBankSemanticRole.ARG4, "A4"),
    ARGM_COM (PropBankSemanticRole.ARGM_COM, "AM-COM"),
    ARGM_DIR (PropBankSemanticRole.ARGM_DIR, "AM-DIR"),
    ARGM_LOC (PropBankSemanticRole.ARGM_LOC, "AM-LOC"),
    ARGM_GOL (PropBankSemanticRole.ARGM_GOL, "AM-GOL"),
    ARGM_MNR (PropBankSemanticRole.ARGM_MNR, "AM-MNR"),
    ARGM_TMP (PropBankSemanticRole.ARGM_TMP, "AM-TMP"),
    ARGM_EXT (PropBankSemanticRole.ARGM_EXT, "AM-EXT"),
    ARGM_REC (PropBankSemanticRole.ARGM_REC, "AM-REC"),
    ARGM_PRD (PropBankSemanticRole.ARGM_PRD, "AM-PRD"),
    ARGM_PRP (PropBankSemanticRole.ARGM_PRP, "AM-PRP"),
    ARGM_PRR (PropBankSemanticRole.ARGM_PRR, "AM-PRR"),
    ARGM_PNC (PropBankSemanticRole.ARGM_PNC, "AM-PNC"),
    ARGM_CAU (PropBankSemanticRole.ARGM_CAU, "AM-CAU"),
    ARGM_DIS (PropBankSemanticRole.ARGM_DIS, "AM-DIS"),
    ARGM_ADV (PropBankSemanticRole.ARGM_ADV, "AM-ADV"),
    ARGM_MOD (PropBankSemanticRole.ARGM_MOD, "AM-MOD"),
    ARGM_NEG (PropBankSemanticRole.ARGM_NEG, "AM-NEG");

    private PropBankSemanticRole propBankSemRole;
    private String clearnlpSemRoleStr;

    ClearnlpSemanticRole(PropBankSemanticRole propBankSemRole, String clearnlpSemRoleStr) {
      this.propBankSemRole = propBankSemRole;
      this.clearnlpSemRoleStr = clearnlpSemRoleStr;
    }

    public PropBankSemanticRole getPropBankSemRole() {
      return propBankSemRole;
    }

    @Override
    public String toString() {
      return clearnlpSemRoleStr;
    }
  }

}
