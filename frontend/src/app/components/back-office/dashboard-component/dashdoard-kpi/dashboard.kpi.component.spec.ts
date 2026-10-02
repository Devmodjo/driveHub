import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DashboardKpiComponent } from './dashboard.kpi.component';

describe('DashboardKpiComponent', () => {
  let component: DashboardKpiComponent;
  let fixture: ComponentFixture<DashboardKpiComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DashboardKpiComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DashboardKpiComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
