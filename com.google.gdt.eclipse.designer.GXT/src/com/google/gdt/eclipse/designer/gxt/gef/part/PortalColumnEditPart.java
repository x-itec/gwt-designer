/*******************************************************************************
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.google.gdt.eclipse.designer.gxt.gef.part;

import com.google.gdt.eclipse.designer.gxt.gef.policy.PortalColumnLayoutEditPolicy;
import com.google.gdt.eclipse.designer.gxt.model.widgets.PortalInfo;
import com.google.gdt.eclipse.designer.gxt.model.widgets.PortalInfo.ColumnInfo;

import org.eclipse.wb.core.gef.policy.selection.NonResizableSelectionEditPolicy;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;

import java.util.List;

/**
 * {@link EditPart} for {@link PortalInfo.ColumnInfo}.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.gef.part
 */
public class PortalColumnEditPart extends GraphicalEditPart {
  protected final ColumnInfo m_column;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public PortalColumnEditPart(ColumnInfo column) {
    m_column = column;
    setModel(m_column);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Policies
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createEditPolicies() {
    super.createEditPolicies();
    installEditPolicy(EditPolicy.SELECTION_ROLE, new NonResizableSelectionEditPolicy());
    installEditPolicy(EditPolicy.LAYOUT_ROLE, new PortalColumnLayoutEditPolicy(m_column));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Figure
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Figure createFigure() {
    return new Figure();
  }

  @Override
  protected void refreshVisuals() {
    Rectangle bounds = m_column.getBounds();
    getFigure().setBounds(bounds);
  }

  @Override
  public Figure getContentPane() {
    return getFigure().getParent();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Children
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<?> getModelChildren() {
    return m_column.getPortlets();
  }

  @Override
  protected void addChildVisual(EditPart childPart, int index) {
    // update "index" to put portlets on Portal figure after Column figure
    {
      Figure portalFigure = getContentPane();
      Figure columnFigure = getFigure();
      int columnFigureIndex = portalFigure.getChildren().indexOf(columnFigure);
      index += columnFigureIndex + 1;
    }
    // do add
    super.addChildVisual(childPart, index);
  }
}
