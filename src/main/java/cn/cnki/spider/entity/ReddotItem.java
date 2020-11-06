package cn.cnki.spider.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReddotItem {
	
	private long id;
	
	private String domain;

	private String itemName;

	private String itemType;

	private String awardType;

	private String awardYear;

	private String manufacturer;

	private String inHouseDesign;

	private String design;

	private String url;

	private long ctime;

	private long utime;

	@Override
	public boolean equals(Object obj) {
		if(this == obj){
			return true;//地址相等
		}

		if(obj == null){
			return false;//非空性：对于任意非空引用x，x.equals(null)应该返回false。
		}

		if(obj instanceof ReddotItem){
			ReddotItem other = (ReddotItem) obj;
			//需要比较的字段相等，则这两个对象相等
			if(this.itemName.equals(other.itemName)
					&& (this.domain.equals(other.domain))
			        && (this.itemType.equals(other.itemType))){
				return true;
			}
		}
		return false;
	}
}
