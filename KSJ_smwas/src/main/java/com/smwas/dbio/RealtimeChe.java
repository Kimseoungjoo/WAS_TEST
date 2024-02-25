package com.smwas.dbio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "realtime_che")
@Data
@NoArgsConstructor
public class RealtimeChe {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "auto_increment")
	private int num;
	
	@Column(name = "tr_key")
	private String trKey;
	
	@Column(name = "output")
	private String output;
	
	// num을 제외한 생성자
    public RealtimeChe(String trKey, String output) {
        this.trKey = trKey;
        this.output = output;
    }

}
