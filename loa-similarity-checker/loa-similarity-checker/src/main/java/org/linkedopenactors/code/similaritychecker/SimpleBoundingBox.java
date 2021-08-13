package org.linkedopenactors.code.similaritychecker;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode()
public class SimpleBoundingBox implements BoundingBox {

	Double latleftTop;
	Double lngleftTop;
	Double latRightBottom;
	Double lngRightBottom;

	public String toString() {
		return latleftTop + ", " + lngleftTop + "," + latRightBottom + "," + lngRightBottom;
	}
}
